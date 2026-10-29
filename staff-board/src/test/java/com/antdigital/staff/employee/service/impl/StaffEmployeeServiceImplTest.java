package com.antdigital.staff.employee.service.impl;

import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.common.util.AesCryptUtil;
import com.antdigital.staff.employee.dto.StaffEmployeeDTO;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.mapper.StaffEmployeeMapper;
import com.antdigital.staff.importbiz.config.ImportProperties;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 员工信息业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class StaffEmployeeServiceImplTest {

    @Mock
    private StaffEmployeeMapper employeeMapper;

    @Mock
    private StaffWhitelistService whitelistService;

    @Mock
    private AesCryptUtil aesCryptUtil;

    @Mock
    private ImportProperties importProperties;

    @InjectMocks
    private StaffEmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        lenient().when(importProperties.isWhitelistEnforced()).thenReturn(true);
    }

    private StaffEmployeeDTO buildDto() {
        StaffEmployeeDTO dto = new StaffEmployeeDTO();
        dto.setEmpNo("E10001");
        dto.setName("张三");
        return dto;
    }

    @Test
    void should_reject_when_paramMissing() {
        // Arrange
        StaffEmployeeDTO dto = new StaffEmployeeDTO();

        // Act & Assert
        assertThatThrownBy(() -> employeeService.create(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void should_reject_when_notInWhitelist() {
        // Arrange
        StaffEmployeeDTO dto = buildDto();
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StaffErrorCode.WL_NOT_IN_WHITELIST.getCode());
    }

    @Test
    void should_reject_when_empNoExists() {
        // Arrange
        StaffEmployeeDTO dto = buildDto();
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(true);
        when(employeeMapper.selectOne(any())).thenReturn(new StaffEmployeeDO());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StaffErrorCode.EMP_NO_EXISTS.getCode());
    }

    @Test
    void should_insert_when_valid() {
        // Arrange
        StaffEmployeeDTO dto = buildDto();
        when(whitelistService.checkInWhitelist("E10001")).thenReturn(true);
        when(employeeMapper.selectOne(any())).thenReturn(null);
        when(aesCryptUtil.encrypt(any())).thenReturn("encrypted");

        // Act
        employeeService.create(dto);

        // Assert
        verify(employeeMapper).insert(any(StaffEmployeeDO.class));
    }

    @Test
    void should_softDelete_when_existing() {
        // Arrange
        StaffEmployeeDO existing = new StaffEmployeeDO();
        existing.setId(1001L);
        existing.setStatus(1);
        when(employeeMapper.selectOne(any())).thenReturn(existing);

        // Act
        employeeService.delete(1001L);

        // Assert
        verify(employeeMapper).updateById(any(StaffEmployeeDO.class));
    }

    @Test
    void should_notDelete_when_notExist() {
        // Arrange
        when(employeeMapper.selectOne(any())).thenReturn(null);

        // Act
        employeeService.delete(1001L);

        // Assert (无副作用，不抛异常)
        verify(employeeMapper, org.mockito.Mockito.never()).updateById(any());
    }
}