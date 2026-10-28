package com.antdigital.staff.importbiz.processor;

import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.antdigital.staff.importbiz.config.ImportProperties;
import com.antdigital.staff.importbiz.excel.EmployeeImportRow;
import com.antdigital.staff.cost.service.StaffCostService;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 导入单行处理器单元测试（核心规则：白名单准入 + 员工 upsert + 预算记录）。
 */
@ExtendWith(MockitoExtension.class)
class StaffImportProcessorTest {

    @Mock
    private StaffWhitelistService whitelistService;

    @Mock
    private StaffEmployeeService employeeService;

    @Mock
    private StaffCostService costService;

    @Mock
    private ImportProperties importProperties;

    @InjectMocks
    private StaffImportProcessor processor;

    @BeforeEach
    void setUp() {
        when(importProperties.isWhitelistEnforced()).thenReturn(true);
    }

    @Test
    void should_reject_when_empNoOrNameBlank() {
        // Arrange
        EmployeeImportRow row = new EmployeeImportRow();
        row.setEmpNo(" ");

        // Act
        String error = processor.processRow(row);

        // Assert
        assertThat(error).contains("不能为空");
    }

    @Test
    void should_reject_when_notInWhitelist() {
        // Arrange
        EmployeeImportRow row = new EmployeeImportRow();
        row.setEmpNo("E10001");
        row.setName("张三");
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(false);

        // Act
        String error = processor.processRow(row);

        // Assert
        assertThat(error).contains("未在白名单");
    }

    @Test
    void should_saveEmployee_when_inWhitelist() {
        // Arrange
        EmployeeImportRow row = new EmployeeImportRow();
        row.setEmpNo("E10001");
        row.setName("张三");
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(true);

        // Act
        String error = processor.processRow(row);

        // Assert
        assertThat(error).isNull();
        verify(employeeService).saveOrUpdateEntity(any(StaffEmployeeDO.class));
    }

    @Test
    void should_recordBudget_when_costFieldsPresent() {
        // Arrange
        EmployeeImportRow row = new EmployeeImportRow();
        row.setEmpNo("E10001");
        row.setName("张三");
        row.setCostType("SALARY");
        row.setCostYear(2026);
        row.setCostMonth(1);
        row.setCostAmount(new java.math.BigDecimal("20000.00"));
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(true);
        StaffEmployeeDO saved = new StaffEmployeeDO();
        saved.setId(1001L);
        saved.setEmpNo("E10001");
        when(employeeService.getEntityByEmpNo("E10001")).thenReturn(saved);
        when(costService.recordBudget(any())).thenReturn(1L);

        // Act
        String error = processor.processRow(row);

        // Assert
        assertThat(error).isNull();
        verify(costService).recordBudget(any());
    }
}