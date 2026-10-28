package com.antdigital.staff.whitelist.service.impl;

import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.whitelist.dto.WhitelistDTO;
import com.antdigital.staff.whitelist.entity.StaffWhitelistDO;
import com.antdigital.staff.whitelist.mapper.StaffWhitelistMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 白名单业务服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class StaffWhitelistServiceImplTest {

    @Mock
    private StaffWhitelistMapper whitelistMapper;

    @InjectMocks
    private StaffWhitelistServiceImpl whitelistService;

    private WhitelistDTO buildDto() {
        WhitelistDTO dto = new WhitelistDTO();
        dto.setEmpNo("E10001");
        dto.setName("张三");
        return dto;
    }

    @Test
    void should_insert_when_notExists() {
        // Arrange
        when(whitelistMapper.selectOne(any())).thenReturn(null);

        // Act
        whitelistService.add(buildDto());

        // Assert
        verify(whitelistMapper).insert(any(StaffWhitelistDO.class));
    }

    @Test
    void should_reject_when_alreadyEnabled() {
        // Arrange
        StaffWhitelistDO existing = new StaffWhitelistDO();
        existing.setId(1L);
        existing.setStatus(1);
        when(whitelistMapper.selectOne(any())).thenReturn(existing);

        // Act & Assert
        assertThatThrownBy(() -> whitelistService.add(buildDto()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", StaffErrorCode.WL_EMP_EXISTS.getCode());
    }

    @Test
    void should_reenable_when_disabled() {
        // Arrange
        StaffWhitelistDO existing = new StaffWhitelistDO();
        existing.setId(1L);
        existing.setStatus(0);
        when(whitelistMapper.selectOne(any())).thenReturn(existing);

        // Act
        Long id = whitelistService.add(buildDto());

        // Assert
        assertThat(id).isEqualTo(1L);
        verify(whitelistMapper).updateById(any(StaffWhitelistDO.class));
    }

    @Test
    void should_returnTrue_when_enabledRecordExists() {
        // Arrange
        when(whitelistMapper.selectCount(any())).thenReturn(1L);

        // Act
        boolean result = whitelistService.checkInWhitelist("E10001");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void should_returnFalse_when_noEnabledRecord() {
        // Arrange
        when(whitelistMapper.selectCount(any())).thenReturn(0L);

        // Act
        boolean result = whitelistService.checkInWhitelist("E10001");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void should_notUpdate_when_removeMissing() {
        // Arrange
        when(whitelistMapper.selectById(1L)).thenReturn(null);

        // Act
        whitelistService.remove(1L);

        // Assert
        verify(whitelistMapper, never()).updateById(any());
    }
}