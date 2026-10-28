package com.antdigital.staff.importbiz.service;

import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.ImportDetailStatusEnum;
import com.antdigital.staff.common.enums.ImportTaskStatusEnum;
import com.antdigital.staff.importbiz.config.ImportProperties;
import com.antdigital.staff.importbiz.entity.StaffImportDetailDO;
import com.antdigital.staff.importbiz.entity.StaffImportTaskDO;
import com.antdigital.staff.importbiz.excel.EmployeeImportRow;
import com.antdigital.staff.importbiz.mapper.StaffImportDetailMapper;
import com.antdigital.staff.importbiz.mapper.StaffImportTaskMapper;
import com.antdigital.staff.importbiz.processor.StaffImportProcessor;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入异步处理器：独立 Bean，使 {@link Async} 通过 Spring 代理生效（避免 self-invocation 失效）。
 */
@Component
public class StaffImportAsyncWorker {

    private static final Logger LOG = LoggerFactory.getLogger(StaffImportAsyncWorker.class);

    private final StaffImportTaskMapper taskMapper;
    private final StaffImportDetailMapper detailMapper;
    private final StaffImportProcessor importProcessor;
    private final ImportProperties importProperties;

    public StaffImportAsyncWorker(StaffImportTaskMapper taskMapper,
                                  StaffImportDetailMapper detailMapper,
                                  StaffImportProcessor importProcessor,
                                  ImportProperties importProperties) {
        this.taskMapper = taskMapper;
        this.detailMapper = detailMapper;
        this.importProcessor = importProcessor;
        this.importProperties = importProperties;
    }

    /**
     * 异步解析与逐行处理。列级错误落明细并更新任务状态（SUCCESS/PARTIAL/FAILED）。
     */
    @Async
    public void processAsync(Long taskId, byte[] fileBytes) {
        StaffImportTaskDO task = taskMapper.selectById(taskId);
        String tenantId = task != null ? task.getTenantId() : null;
        StaffContextHolder.setTenantId(tenantId);
        try {
            markStatus(taskId, ImportTaskStatusEnum.PROCESSING.getCode());
            try {
                List<EmployeeImportRow> rows = new ArrayList<>();
                EasyExcel.read(new ByteArrayInputStream(fileBytes), EmployeeImportRow.class,
                                new PageReadListener<EmployeeImportRow>(rows::addAll))
                        .sheet().doRead();
                if (rows.size() > importProperties.getMaxRows()) {
                    markStatus(taskId, ImportTaskStatusEnum.FAILED.getCode());
                    return;
                }
                updateProgress(taskId, rows.size(), 0, 0, ImportTaskStatusEnum.PROCESSING.getCode());

                int success = 0;
                int fail = 0;
                int rowNo = 0;
                for (EmployeeImportRow row : rows) {
                    rowNo++;
                    String errorMsg;
                    try {
                        errorMsg = importProcessor.processRow(row);
                    } catch (Exception ex) {
                        errorMsg = "处理异常: " + ex.getMessage();
                    }
                    StaffImportDetailDO detail = new StaffImportDetailDO();
                    detail.setTenantId(StaffContextHolder.getTenantId());
                    detail.setTaskId(taskId);
                    detail.setRowNo(rowNo);
                    detail.setEmpNo(row == null ? null : row.getEmpNo());
                    detail.setName(row == null ? null : row.getName());
                    detail.setRawData(null);
                    detail.setStatus(errorMsg == null ? ImportDetailStatusEnum.SUCCESS.getCode()
                            : ImportDetailStatusEnum.FAIL.getCode());
                    detail.setErrorMsg(errorMsg);
                    detailMapper.insert(detail);
                    if (errorMsg == null) {
                        success++;
                    } else {
                        fail++;
                    }
                }

                int status = ImportTaskStatusEnum.SUCCESS.getCode();
                if (fail > 0 && success > 0) {
                    status = ImportTaskStatusEnum.PARTIAL.getCode();
                } else if (fail > 0) {
                    status = ImportTaskStatusEnum.FAILED.getCode();
                }
                updateProgress(taskId, rows.size(), success, fail, status);
            } catch (Exception ex) {
                LOG.error("导入任务处理失败 taskId:{}", taskId, ex);
                markStatus(taskId, ImportTaskStatusEnum.FAILED.getCode());
            }
        } finally {
            StaffContextHolder.clear();
        }
    }

    private void markStatus(Long taskId, int status) {
        StaffImportTaskDO update = new StaffImportTaskDO();
        update.setId(taskId);
        update.setStatus(status);
        taskMapper.updateById(update);
    }

    private void updateProgress(Long taskId, int total, int success, int fail, int status) {
        StaffImportTaskDO update = new StaffImportTaskDO();
        update.setId(taskId);
        update.setTotal(total);
        update.setSuccess(success);
        update.setFail(fail);
        update.setStatus(status);
        taskMapper.updateById(update);
    }
}