package service;

import java.nio.charset.StandardCharsets;

/**
 * 导出服务：F2 接口。
 * 将对应算法结果序列化为 CSV（field,value 两列），返回 UTF-8 字节。
 */
public class ExportService {

    private static final String CRLF = "\r\n";

    /**
     * 根据 tab 生成 CSV 内容。
     * tab 取值：helloworld | hash | bubble。
     * 非法 tab 抛 IllegalArgumentException，由 RouterHandler 转为 400。
     */
    public byte[] export(String tab) {
        String csv;
        if ("helloworld".equals(tab)) {
            csv = buildHelloWorldCsv();
        } else if ("hash".equals(tab)) {
            csv = buildHashCsv();
        } else if ("bubble".equals(tab)) {
            csv = buildBubbleCsv();
        } else {
            throw new IllegalArgumentException("invalid tab");
        }
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * helloworld.csv
     * 表头 field,value
     * result,Hello World
     * timestamp,<时间戳>
     */
    private String buildHelloWorldCsv() {
        long ts = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder(64);
        sb.append("field,value").append(CRLF);
        sb.append("result,Hello World").append(CRLF);
        sb.append("timestamp,").append(ts).append(CRLF);
        return sb.toString();
    }

    /**
     * hash.csv（固定示例输入 hello）
     * 表头 field,value
     * input,hello
     * algorithm,SHA-256
     * hash,<哈希值>
     * length,64
     */
    private String buildHashCsv() {
        HashService svc = new HashService();
        String input = "hello";
        String hash = svc.sha256Hex(input);
        StringBuilder sb = new StringBuilder(128);
        sb.append("field,value").append(CRLF);
        sb.append("input,").append(input).append(CRLF);
        sb.append("algorithm,SHA-256").append(CRLF);
        sb.append("hash,").append(hash).append(CRLF);
        sb.append("length,64").append(CRLF);
        return sb.toString();
    }

    /**
     * bubble.csv（固定示例 5,3,8,1）
     * 表头 field,value
     * input,"[5,3,8,1]"   （含逗号，需加引号包裹）
     * sorted,"[1,3,5,8]"
     * steps,4
     * asc,true
     */
    private String buildBubbleCsv() {
        BubbleSortService svc = new BubbleSortService();
        String json = svc.sort("5,3,8,1");
        String inputArr = extractField(json, "input");
        String sortedArr = extractField(json, "sorted");
        String steps = extractField(json, "steps");
        String asc = extractField(json, "asc");
        StringBuilder sb = new StringBuilder(128);
        sb.append("field,value").append(CRLF);
        sb.append("input,").append(csvQuote(inputArr)).append(CRLF);
        sb.append("sorted,").append(csvQuote(sortedArr)).append(CRLF);
        sb.append("steps,").append(steps).append(CRLF);
        sb.append("asc,").append(asc).append(CRLF);
        return sb.toString();
    }

    /**
     * 从 JSON 字符串中提取指定字段的值。
     * 支持值内含逗号的 JSON 数组（如 [5,3,8,1]）：
     * 当值以 '[' 开头时，配对扫描到匹配的 ']' 结束；
     * 否则沿用逗号/花括号作为值结束符。
     */
    private static String extractField(String json, String field) {
        String key = "\"" + field + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) {
            return "";
        }
        int start = idx + key.length();
        if (start >= json.length()) {
            return "";
        }
        // 数组值：配对扫描方括号，正确处理值内逗号
        if (json.charAt(start) == '[') {
            int depth = 0;
            int end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        end++;
                        break;
                    }
                }
                end++;
            }
            return json.substring(start, end);
        }
        // 普通值：逗号或花括号结束
        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }
        return json.substring(start, end);
    }

    /**
     * CSV 字段值若含逗号/引号/换行，需用双引号包裹并转义内部引号。
     */
    private static String csvQuote(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
