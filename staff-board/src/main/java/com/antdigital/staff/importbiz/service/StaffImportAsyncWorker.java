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
import com.antdigital.staff.importbiz.storage.StorageClient;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
    private final StorageClient storageClient;

    public StaffImportAsyncWorker(StaffImportTaskMapper taskMapper,
                                  StaffImportDetailMapper detailMapper,
                                  StaffImportProcessor importProcessor,
                                  ImportProperties importProperties,
                                  StorageClient storageClient) {
        this.taskMapper = taskMapper;
        this.detailMapper = detailMapper;
        this.importProcessor = importProcessor;
        this.importProperties = importProperties;
        this.storageClient = storageClient;
    }

    /**
     * 异步解析与逐行处理。列级错误落明细并更新任务状态（SUCCESS/PARTIAL/FAILED），
     * 存在失败行时生成并上传错误报告，回写 errorFileUrl。
     *
     * @param tenantId 显式租户上下文（由提交方传入，避免依赖未提交事务读取任务，P0-2）
     */
    @Async
    public void processAsync(Long taskId, String tenantId, byte[] fileBytes) {
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
                updateProgress(taskId, rows.size(), 0, 0, ImportTaskStatusEnum.PROCESSING.getCode(), null);

                int success = 0;
                int fail = 0;
                int rowNo = 0;
                List<String[]> errorRows = new ArrayList<>();
                for (EmployeeImportRow row : rows) {
                    rowNo++;
                    String errorMsg;
                    try {
                        errorMsg = importProcessor.processRow(row);
                    } catch (Exception ex) {
                        LOG.error("导入行处理异常 taskId:{} rowNo:{} empNo:{}", taskId, rowNo,
                                row == null ? null : row.getEmpNo(), ex);
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
                        errorRows.add(new String[]{String.valueOf(rowNo),
                                row == null ? null : row.getEmpNo(),
                                row == null ? null : row.getName(),
                                errorMsg});
                    }
                }

                String errorFileUrl = null;
                if (!errorRows.isEmpty()) {
                    errorFileUrl = uploadErrorReport(errorRows);
                }
                int status = ImportTaskStatusEnum.SUCCESS.getCode();
                if (fail > 0 && success > 0) {
                    status = ImportTaskStatusEnum.PARTIAL.getCode();
                } else if (fail > 0) {
                    status = ImportTaskStatusEnum.FAILED.getCode();
                }
                updateProgress(taskId, rows.size(), success, fail, status, errorFileUrl);
            } catch (Exception ex) {
                LOG.error("导入任务处理失败 taskId:{}", taskId, ex);
                markStatus(taskId, ImportTaskStatusEnum.FAILED.getCode());
            }
        } finally {
            StaffContextHolder.clear();
        }
    }

    private String uploadErrorReport(List<String[]> errorRows) {
        StringBuilder sb = new StringBuilder();
        sb.append(toCsvLine("行号", "工号", "姓名", "失败原因")).append('\n');
        for (String[] row : errorRows) {
            sb.append(toCsvLine(row)).append('\n');
        }
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        return storageClient.upload(new ByteArrayInputStream(content), "staff_import_error_report.csv");
    }

    private String toCsvLine(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String field = fields[i] == null ? "" : fields[i];
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                field = "\"" + field.replace("\"", "\"\"") + "\"";
            }
            sb.append(field);
        }
        return sb.toString();
    }

    private void markStatus(Long taskId, int status) {
        StaffImportTaskDO update = new StaffImportTaskDO();
        update.setId(taskId);
        update.setStatus(status);
        taskMapper.updateById(update);
    }

    private void updateProgress(Long taskId, int total, int success, int fail, int status, String errorFileUrl) {
        StaffImportTaskDO update = new StaffImportTaskDO();
        update.setId(taskId);
        update.setTotal(total);
        update.setSuccess(success);
        update.setFail(fail);
        update.setStatus(status);
        update.setErrorFileUrl(errorFileUrl);
        taskMapper.updateById(update);
    }
}