package demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 导出接口实现：将页面结果以 JSON 文件形式落盘
 */
public class ExportServiceImpl implements ExportService {

    /** 默认导出目录 */
    private static final String DEFAULT_DIR = "export";

    @Override
    public String export(String name, String result, String timestamp) {
        String safeName = name == null || name.isEmpty() ? "result" : name;
        String json = buildJson(safeName, result, timestamp);
        Path dir = Paths.get(DEFAULT_DIR);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(safeName + ".json");
            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + safeName, e);
        }
    }

    @Override
    public String export(String name, String result) {
        return export(name, result, now());
    }

    /**
     * 构建 JSON 字符串，与前端展示/导出格式保持一致
     */
    private String buildJson(String name, String result, String timestamp) {
        return "{"
                + "\"name\":\"" + escape(name) + "\","
                + "\"result\":" + toJsonValue(result) + ","
                + "\"timestamp\":\"" + escape(timestamp) + "\""
                + "}";
    }

    private String toJsonValue(String result) {
        if (result == null) {
            return "null";
        }
        // 若结果本身是合法 JSON（以 { 或 [ 开头），直接嵌入；否则按字符串处理
        String trimmed = result.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed;
        }
        return "\"" + escape(result) + "\"";
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private String now() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }
}
