package com.antdigital.staff.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AES 字段加解密工具类单元测试（使用合法 Base64 的 AES-128 密钥）。
 */
class AesCryptUtilTest {

    /** c3RhZmYtYm9hcmQta2V5MQ== 解码后为 16 字节，合法 AES-128 密钥。 */
    private final AesCryptUtil cryptUtil = new AesCryptUtil("c3RhZmYtYm9hcmQta2V5MQ==");

    @Test
    void should_encryptAndDecrypt_when_roundTrip() {
        // Arrange
        String plain = "110101199001011234";

        // Act
        String encrypted = cryptUtil.encrypt(plain);
        String decrypted = cryptUtil.decrypt(encrypted);

        // Assert
        assertThat(encrypted).isNotEqualTo(plain);
        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    void should_returnEmpty_when_inputEmpty() {
        // Act & Assert
        assertThat(cryptUtil.encrypt("")).isEmpty();
        assertThat(cryptUtil.decrypt("")).isEmpty();
    }

    @Test
    void should_returnNull_when_inputNull() {
        // Act & Assert
        assertThat(cryptUtil.encrypt(null)).isNull();
        assertThat(cryptUtil.decrypt(null)).isNull();
    }
}