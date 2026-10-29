package com.antdigital.staff.importbiz.service.impl;

import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.ImportTaskStatusEnum;
import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.importbiz.config.ImportProperties;
import com.antdigital.staff.importbiz.entity.StaffImportTaskDO;
import com.antdigital.staff.importbiz.mapper.StaffImportTaskMapper;
import com.antdigital.staff.importbiz.service.StaffImportAsyncWorker;
import com.antdigital.staff.importbiz.service.StaffImportService;
import com.antdigital.staff.importbiz.storage.StorageClient;
import com.antdigital.staff.importbiz.vo.ImportTaskVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 员工批量导入业务服务实现：上传入库 → 异步逐行处理（委托 {@link StaffImportAsyncWorker} 规避 self-invocation）。
 */
@Service
public class StaffImportServiceImpl implements StaffImportService {

    private static final Logger LOG = LoggerFactory.getLogger(StaffImportServiceImpl.class);

    private final StaffImportTaskMapper taskMapper;
    private final StorageClient storageClient;
    private final ImportProperties importProperties;
    private final StaffImportAsyncWorker asyncWorker;
    private final long processingTimeoutMinutes;

    @Autowired
    public StaffImportServiceImpl(StaffImportTaskMapper taskMapper,
                                  StorageClient storageClient,
                                  ImportProperties importProperties,
                                  StaffImportAsyncWorker asyncWorker,
                                  @Value("${staff.import.processing-timeout-minutes:30}") long processingTimeoutMinutes) {
        this.taskMapper = taskMapper;
        this.storageClient = storageClient;
        this.importProperties = importProperties;
        this.asyncWorker = asyncWorker;
        this.processingTimeoutMinutes = processingTimeoutMinutes;
    }

    @Override
    public Long submitImport(byte[] fileBytes, String fileName, String operator) {
        if (!importProperties.isEnableImport()) {
            throw new BusinessException(StaffErrorCode.IMP_FILE_INVALID, "导入功能已关闭");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(StaffErrorCode.IMP_FILE_INVALID);
        }
        if (!isXlsx(fileName)) {
            throw new BusinessException(StaffErrorCode.IMP_FILE_INVALID);
        }

        String tenantId = StaffContextHolder.getTenantId();
        // 任务级幂等：文件摘要 + 租户（design §5.3.3.1）。摘要用 SHA-256，避免审计误报（P2-6）。
        String digest = sha256(fileBytes);
        StaffImportTaskDO existing = findTaskByDigest(tenantId, digest);
        if (existing != null) {
            return handleExistingTask(existing, tenantId, fileBytes);
        }

        // 事务外先落文件，再落任务记录，避免文件 I/O 落入长事务（G3.2）
        String fileUrl = storageClient.upload(new ByteArrayInputStream(fileBytes), fileName);

        StaffImportTaskDO task = new StaffImportTaskDO();
        task.setTenantId(tenantId);
        task.setFileName(fileName);
        task.setFileUrl(fileUrl);
        task.setFileMd5(digest);
        task.setTotal(0);
        task.setSuccess(0);
        task.setFail(0);
        task.setStatus(ImportTaskStatusEnum.PENDING.getCode());
        task.setOperator(operator);
        try {
            taskMapper.insert(task);
        } catch (DuplicateKeyException ex) {
            LOG.warn("导入任务幂等键竞争，回查既有任务 tenantId:{}", tenantId, ex);
            StaffImportTaskDO race = findTaskByDigest(tenantId, digest);
            if (race != null) {
                return handleExistingTask(race, tenantId, fileBytes);
            }
            throw new BusinessException(StaffErrorCode.SYSTEM_ERROR, "重复导入任务冲突");
        }
        Long taskId = task.getId();
        // 显式下发 tenantId，避免异步线程依赖未提交事务读任务导致租户回退 default
        asyncWorker.processAsync(taskId, tenantId, fileBytes);
        return taskId;
    }

    /**
     * 幂等命中后按状态分流（P0-2）：SUCCESS/PARTIAL 已完成 → 幂等复用；
     * FAILED 或 PROCESSING/PENDING 卡死超时 → 复用任务重置并重投；其余进行中 → 直接返回既有任务。
     */
    private Long handleExistingTask(StaffImportTaskDO task, String tenantId, byte[] fileBytes) {
        int status = task.getStatus() == null ? ImportTaskStatusEnum.PENDING.getCode() : task.getStatus();
        boolean completed = status == ImportTaskStatusEnum.SUCCESS.getCode()
                || status == ImportTaskStatusEnum.PARTIAL.getCode();
        if (completed) {
            LOG.info("重复提交已完成导入任务，幂等返回 taskId:{} status:{}", task.getId(), status);
            return task.getId();
        }
        boolean retriable = status == ImportTaskStatusEnum.FAILED.getCode() || isStale(task);
        if (!retriable) {
            LOG.info("导入任务处理中，幂等返回 taskId:{} status:{}", task.getId(), status);
            return task.getId();
        }
        LOG.warn("重投可重试导入任务 taskId:{} 原状态:{} 原因:{}", task.getId(), status,
                status == ImportTaskStatusEnum.FAILED.getCode() ? "失败" : "处理中卡死超时");
        resetToPending(task.getId());
        asyncWorker.processAsync(task.getId(), tenantId, fileBytes);
        return task.getId();
    }

    private boolean isStale(StaffImportTaskDO task) {
        LocalDateTime modified = task.getGmtModified() != null ? task.getGmtModified() : task.getGmtCreate();
        if (modified == null) {
            return false;
        }
        return modified.isBefore(LocalDateTime.now().minusMinutes(processingTimeoutMinutes));
    }

    private void resetToPending(Long taskId) {
        StaffImportTaskDO update = new StaffImportTaskDO();
        update.setId(taskId);
        update.setStatus(ImportTaskStatusEnum.PENDING.getCode());
        update.setErrorFileUrl(null);
        taskMapper.updateById(update);
    }

    @Override
    public ImportTaskVO getTaskResult(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(StaffErrorCode.IMP_TASK_NOT_FOUND);
        }
        // 资源操作 tenant_id + 资源ID 双重校验，防跨租户越权（G14.2）
        LambdaQueryWrapper<StaffImportTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffImportTaskDO::getId, taskId);
        wrapper.eq(StaffImportTaskDO::getTenantId, StaffContextHolder.getTenantId());
        StaffImportTaskDO task = taskMapper.selectOne(wrapper);
        if (task == null) {
            throw new BusinessException(StaffErrorCode.IMP_TASK_NOT_FOUND);
        }
        ImportTaskVO vo = new ImportTaskVO();
        vo.setTaskId(task.getId());
        vo.setStatus(task.getStatus());
        vo.setTotal(task.getTotal());
        vo.setSuccess(task.getSuccess());
        vo.setFail(task.getFail());
        vo.setErrorFileUrl(task.getErrorFileUrl());
        return vo;
    }

    private boolean isXlsx(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
    }

    private StaffImportTaskDO findTaskByDigest(String tenantId, String digest) {
        LambdaQueryWrapper<StaffImportTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffImportTaskDO::getTenantId, tenantId);
        wrapper.eq(StaffImportTaskDO::getFileMd5, digest);
        wrapper.last("LIMIT 1");
        return taskMapper.selectOne(wrapper);
    }

    /** 计算文件 SHA-256（64 位十六进制），仅作为 (tenant_id, file_md5) 任务级幂等摘要，非密码学签名场景（P2-6）。 */
    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("SHA-256 摘要算法不可用", ex);
            throw new IllegalStateException("SHA-256 算法不可用", ex);
        }
    }
}