package algoapi.controller;

import algoapi.model.ApiResult;
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
    public ApiResult hello() {
        Map<String, Object> data = new HashMap<>();
        data.put("result", "HelloWorld");
        return ApiResult.ok(data);
    }

    /**
     * 哈希算法接口（SHA-256）
     * input 为空时回退默认值 "hello"
     */
    @GetMapping("/hash")
    public ApiResult hash(@RequestParam(defaultValue = "hello") String input) {
        String[] result = hashService.hash(input);
        Map<String, Object> data = new HashMap<>();
        data.put("input", escapeHtml(input));
        data.put("algorithm", result[0]);
        data.put("hash", result[1]);
        return ApiResult.ok(data);
    }

    /**
     * 冒泡排序接口
     * nums 格式非法时回退默认数组 [5,3,8,1,9,2] 并标记 warning
     */
    @GetMapping("/bubble")
    public ApiResult bubble(@RequestParam(defaultValue = "5,3,8,1,9,2") String nums) {
        int[] input;
        Map<String, Object> data = new HashMap<>();
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
            data.put("warning", "输入非法，已使用默认数组");
        }
        int[] sorted = bubbleSortService.sort(input);
        data.put("input", input);
        data.put("sorted", sorted);
        return ApiResult.ok(data);
    }

    /**
     * 导出接口：按 type 导出 CSV 文件
     * 支持可选 input（hash 类型）/ nums（bubble 类型）透传用户动态参数，
     * 保证导出内容与页面展示一致；参数缺失/非法时回退默认值。
     * type 非法时由全局异常处理器兜底返回 400
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "") String input,
            @RequestParam(required = false, defaultValue = "") String nums) {
        byte[] csv = exportService.exportCsv(type, input, nums);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    /**
     * HTML 字符转义（纵深防御，配合前端 textContent）
     * B1: 对 hash 接口回显的 input 做基本 HTML 字符转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
