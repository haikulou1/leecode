package algoapi.controller;

import algoapi.service.BubbleSortService;
import algoapi.service.ExportService;
import algoapi.service.HashService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 算法接口控制器
 * /api/hello   - HelloWorld
 * /api/hash    - 哈希算法（SHA-256）
 * /api/bubble  - 冒泡排序
 * /api/export  - 导出 CSV
 */
@RestController
@RequestMapping("/api")
public class AlgoController {

    private final HashService hashService;
    private final BubbleSortService bubbleSortService;
    private final ExportService exportService;

    public AlgoController(HashService hashService, BubbleSortService bubbleSortService, ExportService exportService) {
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
        this.exportService = exportService;
    }

    /**
     * HelloWorld 接口
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "HelloWorld");
        return result;
    }

    /**
     * 哈希算法接口（SHA-256）
     * input 为空时回退默认值 "hello"
     */
    @GetMapping("/hash")
    public Map<String, Object> hash(@RequestParam(defaultValue = "hello") String input) {
        String[] result = hashService.hash(input);
        Map<String, Object> map = new HashMap<>();
        map.put("input", input);
        map.put("algorithm", result[0]);
        map.put("hash", result[1]);
        return map;
    }

    /**
     * 冒泡排序接口
     * nums 格式非法时回退默认数组 [5,3,8,1,9,2] 并标记 warning
     */
    @GetMapping("/bubble")
    public Map<String, Object> bubble(@RequestParam(defaultValue = "5,3,8,1,9,2") String nums) {
        int[] input;
        Map<String, Object> map = new HashMap<>();
        try {
            input = Arrays.stream(nums.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
            if (input.length == 0) {
                throw new NumberFormatException("空数组");
            }
        } catch (NumberFormatException e) {
            // 兜底：回退默认数组
            input = new int[]{5, 3, 8, 1, 9, 2};
            map.put("warning", "输入非法，已使用默认数组");
        }
        int[] sorted = bubbleSortService.sort(input);
        map.put("input", input);
        map.put("sorted", sorted);
        return map;
    }

    /**
     * 导出接口：按 type 导出 CSV 文件
     * type 非法时由全局异常处理器兜底返回 400
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam String type) {
        byte[] csv = exportService.exportCsv(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
