package com.antdigital.staff.employee.controller;

import com.antdigital.staff.common.model.ApiResponse;
import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.employee.dto.StaffEmployeeDTO;
import com.antdigital.staff.employee.dto.StaffQueryDTO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.antdigital.staff.employee.vo.StaffEmployeeVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工信息 oneapi 接口（W01-W05）。
 */
@RestController
@RequestMapping("/api/staff/employee")
public class StaffEmployeeController {

    private final StaffEmployeeService employeeService;

    @Autowired
    public StaffEmployeeController(StaffEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /** W01 录入员工基本信息。 */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody StaffEmployeeDTO dto) {
        Long employeeId = employeeService.create(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("employeeId", employeeId);
        return ApiResponse.success(data);
    }

    /** W02 修改员工基本信息。 */
    @PutMapping("/{employeeId}")
    public ApiResponse<Void> update(@PathVariable Long employeeId, @Valid @RequestBody StaffEmployeeDTO dto) {
        employeeService.update(employeeId, dto);
        return ApiResponse.success(null);
    }

    /** W03 删除员工（逻辑删除）。 */
    @DeleteMapping("/{employeeId}")
    public ApiResponse<Void> delete(@PathVariable Long employeeId) {
        employeeService.delete(employeeId);
        return ApiResponse.success(null);
    }

    /** W04 查询员工详情。 */
    @GetMapping("/{employeeId}")
    public ApiResponse<StaffEmployeeVO> getDetail(@PathVariable Long employeeId) {
        return ApiResponse.success(employeeService.getDetail(employeeId));
    }

    /** W05 分页查询员工列表。 */
    @PostMapping("/list")
    public ApiResponse<PageResult<StaffEmployeeVO>> pageQuery(@RequestBody StaffQueryDTO query) {
        return ApiResponse.success(employeeService.pageQuery(query));
    }
}