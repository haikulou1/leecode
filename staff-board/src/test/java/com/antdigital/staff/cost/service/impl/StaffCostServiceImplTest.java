package com.antdigital.staff.cost.service.impl;

import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.cost.dto.CostBudgetDTO;
import com.antdigital.staff.cost.entity.StaffCostBudgetDO;
import com.antdigital.staff.cost.mapper.StaffCostBudgetMapper;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 成本预算业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class StaffCostServiceImplTest {

    @Mock
    private StaffCostBudgetMapper costBudgetMapper;

    @Mock
    private StaffEmployeeService employeeService;

    @InjectMocks
    private StaffCostServiceImpl costService;

    @Test
    void should_reject_when_employeeNotFound() {
        // Arrange
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setEmployeeId(1001L);
        dto.setYear(2026);
        dto.setMonth(1);
        dto.setCostType("SALARY");
        dto.setAmount(new BigDecimal("20000.00"));
        when(employeeService.getEntityById(1001L)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> costService.recordBudget(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StaffErrorCode.COST_EMP_NOT_FOUND.getCode());
    }

    @Test
    void should_reject_when_amountNonPositive() {
        // Arrange
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setEmployeeId(1001L);
        dto.setYear(2026);
        dto.setMonth(1);
        dto.setCostType("SALARY");
        dto.setAmount(new BigDecimal("0.00"));

        // Act & Assert
        assertThatThrownBy(() -> costService.recordBudget(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void should_insertNewBudget_when_dimensionNotExists() {
        // Arrange
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setEmployeeId(1001L);
        dto.setYear(2026);
        dto.setMonth(1);
        dto.setCostType("SALARY");
        dto.setAmount(new BigDecimal("20000.00"));
        StaffEmployeeDO employee = new StaffEmployeeDO();
        employee.setId(1001L);
        employee.setEmpNo("E10001");
        when(employeeService.getEntityById(1001L)).thenReturn(employee);
        when(costBudgetMapper.selectOne(any())).thenReturn(null);

        // Act
        costService.recordBudget(dto);

        // Assert (mock 环境下 insert 不自增回填 ID，仅验证插入动作与参数)
        verify(costBudgetMapper).insert(any(StaffCostBudgetDO.class));
    }

    @Test
    void should_updateExisting_when_dimensionExists() {
        // Arrange
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setEmployeeId(1001L);
        dto.setYear(2026);
        dto.setMonth(1);
        dto.setCostType("SALARY");
        dto.setAmount(new BigDecimal("25000.00"));
        StaffEmployeeDO employee = new StaffEmployeeDO();
        employee.setId(1001L);
        employee.setEmpNo("E10001");
        StaffCostBudgetDO existing = new StaffCostBudgetDO();
        existing.setId(3001L);
        when(employeeService.getEntityById(1001L)).thenReturn(employee);
        when(costBudgetMapper.selectOne(any())).thenReturn(existing);

        // Act
        Long budgetId = costService.recordBudget(dto);

        // Assert
        verify(costBudgetMapper).updateById(any(StaffCostBudgetDO.class));
        assertThat(budgetId).isEqualTo(3001L);
    }
}