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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 员工批量导入业务服务实现：上传入库 → 异步逐行处理（委托 {@link StaffImportAsyncWorker} 规避 self-invocation）。
 */
@Service
public class StaffImportServiceImpl implements StaffImportService {

    private final StaffImportTaskMapper taskMapper;
    private final StorageClient storageClient;
    private final ImportProperties importProperties;
    private final StaffImportAsyncWorker asyncWorker;

    @Autowired
    public StaffImportServiceImpl(StaffImportTaskMapper taskMapper,
                                  StorageClient storageClient,
                                  ImportProperties importProperties,
                                  StaffImportAsyncWorker asyncWorker) {
        this.taskMapper = taskMapper;
        this.storageClient = storageClient;
        this.importProperties = importProperties;
        this.asyncWorker = asyncWorker;
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
        // 任务级幂等：文件 MD5 + 租户（design §5.3.3.1）
        String fileMd5 = md5(fileBytes);
        Long existingTaskId = findTaskIdByMd5(tenantId, fileMd5);
        if (existingTaskId != null) {
            return existingTaskId;
        }

        // 事务外先落文件，再落任务记录，避免文件 I/O 落入长事务（G3.2/P1-2）
        String fileUrl = storageClient.upload(new ByteArrayInputStream(fileBytes), fileName);

        StaffImportTaskDO task = new StaffImportTaskDO();
        task.setTenantId(tenantId);
        task.setFileName(fileName);
        task.setFileUrl(fileUrl);
        task.setFileMd5(fileMd5);
        task.setTotal(0);
        task.setSuccess(0);
        task.setFail(0);
        task.setStatus(ImportTaskStatusEnum.PENDING.getCode());
        task.setOperator(operator);
        try {
            taskMapper.insert(task);
        } catch (DuplicateKeyException ex) {
            Long raceTaskId = findTaskIdByMd5(tenantId, fileMd5);
            if (raceTaskId != null) {
                return raceTaskId;
            }
            throw new BusinessException(StaffErrorCode.SYSTEM_ERROR, "重复导入任务冲突");
        }
        Long taskId = task.getId();
        // 显式下发 tenantId，避免异步线程依赖未提交事务读任务导致租户回退 default（P0-2）
        asyncWorker.processAsync(taskId, tenantId, fileBytes);
        return taskId;
    }

    @Override
    public ImportTaskVO getTaskResult(Long taskId) {
        if (taskId == null) {
            throw new BusinessException(StaffErrorCode.IMP_TASK_NOT_FOUND);
        }
        // 资源操作 tenant_id + 资源ID 双重校验，防跨租户越权（P0-1/G14.2）
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

    private Long findTaskIdByMd5(String tenantId, String fileMd5) {
        LambdaQueryWrapper<StaffImportTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffImportTaskDO::getTenantId, tenantId);
        wrapper.eq(StaffImportTaskDO::getFileMd5, fileMd5);
        wrapper.last("LIMIT 1");
        StaffImportTaskDO task = taskMapper.selectOne(wrapper);
        return task != null ? task.getId() : null;
    }

    /** 计算文件 MD5（32 位十六进制），仅作为 (tenant_id, file_md5) 任务级幂等摘要使用，非密码学签名场景。 */
    private String md5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(data));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("MD5 算法不可用", ex);
        }
    }
}