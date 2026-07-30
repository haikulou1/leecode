package algoapi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HashService 单元测试
 * 验证算法正确性 + 边界：空串
 * I2: 满足 clarify.md §4.6 测试要求
 */
class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    void hash_normalString_returnsSha256() {
        String[] result = hashService.hash("hello");
        assertEquals("SHA-256", result[0]);
        assertNotNull(result[1]);
        // SHA-256 hex 长度 = 64
        assertEquals(64, result[1].length());
    }

    @Test
    void hash_emptyString_returnsValidHash() {
        String[] result = hashService.hash("");
        assertEquals("SHA-256", result[0]);
        assertNotNull(result[1]);
        assertFalse(result[1].isEmpty());
    }

    @Test
    void hash_sameInput_producesSameHash() {
        String[] r1 = hashService.hash("test");
        String[] r2 = hashService.hash("test");
        assertEquals(r1[1], r2[1]);
    }

    @Test
    void hash_differentInput_producesDifferentHash() {
        String[] r1 = hashService.hash("input1");
        String[] r2 = hashService.hash("input2");
        assertNotEquals(r1[1], r2[1]);
    }
}
