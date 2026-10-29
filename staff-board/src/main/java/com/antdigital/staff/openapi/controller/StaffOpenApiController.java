package com.antdigital.staff.openapi.controller;

import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.common.model.ApiResponse;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.antdigital.staff.employee.vo.StaffEmployeeVO;
import com.antdigital.staff.importbiz.service.StaffImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 对外 OpenAPI 接口（O01-O02），供可信外部系统调用。
 * 说明：签名验签/租户识别由统一网关完成，本层仅暴露最小契约。
 */
@RestController
@RequestMapping("/openapi/staff")
public class StaffOpenApiController {

    private final StaffImportService importService;
    private final StaffEmployeeService employeeService;

    @Autowired
    public StaffOpenApiController(StaffImportService importService,
                                  StaffEmployeeService employeeService) {
        this.importService = importService;
        this.employeeService = employeeService;
    }

    /** O01 外部批量导入员工。 */
    @PostMapping("/employee/import")
    public ApiResponse<Map<String, Long>> externalImport(@RequestParam("file") MultipartFile file) throws IOException {
        Long taskId = importService.submitImport(file.getBytes(), file.getOriginalFilename(), null);
        Map<String, Long> data = new HashMap<>();
        data.put("taskId", taskId);
        return ApiResponse.success(data);
    }

    /** O02 按工号查询员工（脱敏返回）。 */
    @GetMapping("/employee/{empNo}")
    public ApiResponse<StaffEmployeeVO> getByEmpNo(@PathVariable String empNo) {
        StaffEmployeeDO employee = employeeService.getEntityByEmpNo(empNo);
        if (employee == null) {
            // 员工不存在返回明确错误码，避免 data=null 语义歧义（P1-7）
            throw new BusinessException(StaffErrorCode.EMP_NO_NOT_EXISTS);
        }
        return ApiResponse.success(employeeService.getDetail(employee.getId()));
    }
}