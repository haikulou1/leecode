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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;

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
    @Transactional(rollbackFor = Exception.class)
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
        String fileUrl = storageClient.upload(new ByteArrayInputStream(fileBytes), fileName);

        StaffImportTaskDO task = new StaffImportTaskDO();
        task.setTenantId(StaffContextHolder.getTenantId());
        task.setFileName(fileName);
        task.setFileUrl(fileUrl);
        task.setTotal(0);
        task.setSuccess(0);
        task.setFail(0);
        task.setStatus(ImportTaskStatusEnum.PENDING.getCode());
        task.setOperator(operator);
        taskMapper.insert(task);
        Long taskId = task.getId();
        asyncWorker.processAsync(taskId, fileBytes);
        return taskId;
    }

    @Override
    public ImportTaskVO getTaskResult(Long taskId) {
        StaffImportTaskDO task = taskMapper.selectById(taskId);
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
}