package com.antdigital.staff.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 脱敏工具类单元测试。
 */
class MaskUtilTest {

    @Test
    void should_maskPhone_when_phoneIsValid() {
        // Act
        String masked = MaskUtil.maskPhone("13800008000");

        // Assert
        assertThat(masked).isEqualTo("138****8000");
    }

    @Test
    void should_returnOriginalPhone_when_phoneIsTooShort() {
        // Act
        String masked = MaskUtil.maskPhone("123");

        // Assert
        assertThat(masked).isEqualTo("123");
    }

    @Test
    void should_maskIdCard_when_idCardIsValid() {
        // Act
        String masked = MaskUtil.maskIdCard("110101199001011234");

        // Assert
        assertThat(masked).isEqualTo("110101********1234");
    }

    @Test
    void should_returnStar_when_idCardIsTooShort() {
        // Act
        String masked = MaskUtil.maskIdCard("12345");

        // Assert
        assertThat(masked).isEqualTo("****");
    }

    @Test
    void should_returnNull_when_inputIsNull() {
        // Act & Assert
        assertThat(MaskUtil.maskPhone(null)).isNull();
        assertThat(MaskUtil.maskIdCard(null)).isNull();
    }
}