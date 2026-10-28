package com.antdigital.staff.importbiz.service;

import com.antdigital.staff.importbiz.vo.ImportTaskVO;

/**
 * 员工批量导入业务服务。
 */
public interface StaffImportService {

    /** 提交批量导入文件，返回任务ID（异步处理）。 */
    Long submitImport(byte[] fileBytes, String fileName, String operator);

    /** 查询导入任务结果。 */
    ImportTaskVO getTaskResult(Long taskId);
}