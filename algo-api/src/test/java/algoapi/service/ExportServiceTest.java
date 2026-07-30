package algoapi.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExportService 单元测试
 * 验证 3 种 type 的 CSV 输出 + 非法 type 抛 IllegalArgumentException
 * I2: 满足 clarify.md §4.6 测试要求
 */
class ExportServiceTest {

    private final HashService hashService = new HashService();
    private final BubbleSortService bubbleSortService = new BubbleSortService();
    private final ExportService exportService = new ExportService(hashService, bubbleSortService);

    @Test
    void exportCsv_hello_returnsResultHeaderAndValue() {
        String csv = new String(exportService.exportCsv("hello", "", ""), StandardCharsets.UTF_8);
        assertTrue(csv.contains("result"));
        assertTrue(csv.contains("HelloWorld"));
    }

    @Test
    void exportCsv_hash_returnsInputAlgorithmHash() {
        String csv = new String(exportService.exportCsv("hash", "hello", ""), StandardCharsets.UTF_8);
        assertTrue(csv.contains("input,algorithm,hash"));
        assertTrue(csv.contains("hello"));
        assertTrue(csv.contains("SHA-256"));
    }

    @Test
    void exportCsv_bubble_returnsIndexInputSorted() {
        String csv = new String(exportService.exportCsv("bubble", "", "5,3,8,1,9,2"), StandardCharsets.UTF_8);
        assertTrue(csv.contains("index,input,sorted"));
        // 默认数组排序后应含 1
        assertTrue(csv.contains("1"));
    }

    @Test
    void exportCsv_invalidType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> exportService.exportCsv("invalid", "", ""));
    }

    /**
     * N3 验证：CSV 防注入——以 = 开头的字段值加单引号前缀
     */
    @Test
    void exportCsv_hashWithInjectionAttempt_isSanitized() {
        String csv = new String(exportService.exportCsv("hash", "=cmd|'/c calc'!A1", ""), StandardCharsets.UTF_8);
        // 防注入后应以 ' 前缀开头
        assertTrue(csv.contains("'=cmd"));
    }
}
