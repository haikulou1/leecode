package algoapi.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 导出服务：按 type 路由生成 CSV 字节
 * hello → result 标题行 + HelloWorld
 * hash  → input,algorithm,hash 表头 + 默认 input 的哈希结果
 * bubble → index,input,sorted 表头 + 逐元素行
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
     * @param type hello | hash | bubble
     * @return CSV 文本字节
     * @throws IllegalArgumentException type 非法时抛出（由全局处理器兜底 400）
     */
    public byte[] exportCsv(String type) {
        String csv;
        switch (type) {
            case "hello":
                csv = "result\nHelloWorld\n";
                break;
            case "hash":
                String[] result = hashService.hash("hello");
                csv = "input,algorithm,hash\n" + "hello," + result[0] + "," + result[1] + "\n";
                break;
            case "bubble":
                int[] input = {5, 3, 8, 1, 9, 2};
                int[] sorted = bubbleSortService.sort(input);
                StringBuilder sb = new StringBuilder();
                sb.append("index,input,sorted\n");
                for (int i = 0; i < input.length; i++) {
                    sb.append(i).append(",").append(input[i]).append(",").append(sorted[i]).append("\n");
                }
                csv = sb.toString();
                break;
            default:
                throw new IllegalArgumentException("不支持的导出类型: " + type + "，可选: hello, hash, bubble");
        }
        return csv.getBytes(StandardCharsets.UTF_8);
    }
}
