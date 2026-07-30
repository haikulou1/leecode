package algoapi.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 导出服务：按 type 路由生成 CSV 字节
 * hello → result 标题行 + HelloWorld
 * hash  → input,algorithm,hash 表头 + 指定 input 的哈希结果
 * bubble → index,input,sorted 表头 + 逐元素行
 *
 * 可选 input/nums 透传用户动态参数，保证导出内容与页面展示一致；
 * 参数缺失/非法时回退默认值（与 /api/hash、/api/bubble 接口兜底逻辑一致）。
 */
@Service
public class ExportService {

    private final HashService hashService;
    private final BubbleSortService bubbleSortService;

    public ExportService(HashService hashService, BubbleSortService bubbleSortService) {
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
    }

    /**
     * 按 type 生成 CSV 字节
     *
     * @param type  hello | hash | bubble
     * @param input hash 类型的输入字符串，为空/null 回退 "hello"
     * @param nums  bubble 类型的数字序列字符串，为空/null/非法回退默认数组 [5,3,8,1,9,2]
     * @return CSV 文本字节
     * @throws IllegalArgumentException type 非法时抛出（由全局处理器兜底 400）
     */
    public byte[] exportCsv(String type, String input, String nums) {
        String csv;
        switch (type) {
            case "hello":
                csv = "result\n" + sanitize("HelloWorld") + "\n";
                break;
            case "hash":
                String actualInput = (input == null || input.isEmpty()) ? "hello" : input;
                String[] result = hashService.hash(actualInput);
                csv = "input,algorithm,hash\n" + sanitize(actualInput) + "," + sanitize(result[0]) + "," + sanitize(result[1]) + "\n";
                break;
            case "bubble":
                int[] arr = parseBubbleNums(nums);
                int[] sorted = bubbleSortService.sort(arr);
                StringBuilder sb = new StringBuilder();
                sb.append("index,input,sorted\n");
                for (int i = 0; i < arr.length; i++) {
                    sb.append(i).append(",").append(arr[i]).append(",").append(sorted[i]).append("\n");
                }
                csv = sb.toString();
                break;
            default:
                throw new IllegalArgumentException("不支持的导出类型: " + type + "，可选: hello, hash, bubble");
        }
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 解析 bubble 数字序列，与 AlgoController.bubble 兜底逻辑一致
     */
    private int[] parseBubbleNums(String nums) {
        try {
            int[] arr = Arrays.stream(nums.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
            if (arr.length == 0) {
                throw new NumberFormatException("空数组");
            }
            return arr;
        } catch (NumberFormatException e) {
            // 兜底：回退默认数组
            return new int[]{5, 3, 8, 1, 9, 2};
        }
    }

    /**
     * N3: CSV 防注入处理——字段值以 =,+,-,@ 开头时加单引号前缀
     * 防御 CSV/Formula Injection（Excel 公式注入）
     */
    private String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
    }
}
