package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ApiServer;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * GET /api/export?type=helloworld|hash|bubble-sort&format=csv
 * 出参：text/csv 字节流，Content-Disposition: attachment; filename=<type>-result.csv
 * 实现要点：复用三个 handler 的静态业务逻辑生成数据，再转 CSV。
 */
public class ExportHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        if (ApiServer.handleOptions(exchange)) {
            return;
        }
        try {
            Map<String, String> params = ApiServer.parseQuery(exchange.getRequestURI());
            String type = params.getOrDefault("type", "helloworld");
            String format = params.getOrDefault("format", "csv");
            if (!"csv".equalsIgnoreCase(format)) {
                ApiServer.writeError(exchange, 400, "unsupported format: " + format);
                return;
            }
            String csv = toCsv(type);
            byte[] body = csv.getBytes("UTF-8");
            String filename = type + "-result.csv";
            ApiServer.applyCors(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + filename);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        } catch (Exception e) {
            ApiServer.writeError(exchange, 500, "internal error: " + e.getMessage());
        }
    }

    /** 按 type 生成 CSV 内容 */
    private String toCsv(String type) throws Exception {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case "helloworld": {
                String message = HelloWorldHandler.getMessage();
                sb.append("field,value\n");
                sb.append(csvField("message")).append(",").append(csvField(message)).append("\n");
                break;
            }
            case "hash": {
                // 导出默认示例：空串 + sha256（复用 HashHandler 计算逻辑）
                String input = "";
                String algo = "sha256";
                String hash = HashHandler.computeHash(input, algo);
                sb.append("input,algo,hash\n");
                sb.append(csvField(input)).append(",")
                        .append(csvField(algo)).append(",")
                        .append(csvField(hash)).append("\n");
                break;
            }
            case "bubble-sort": {
                int[] original = BubbleSortHandler.generateRandom();
                int[] sorted = BubbleSortHandler.bubbleSort(Arrays.copyOf(original, original.length));
                sb.append("index,original,sorted\n");
                for (int i = 0; i < original.length; i++) {
                    sb.append(i).append(",")
                            .append(original[i]).append(",")
                            .append(sorted[i]).append("\n");
                }
                break;
            }
            default:
                sb.append("error,unknown type: ").append(csvField(type)).append("\n");
        }
        return sb.toString();
    }

    /** CSV 字段转义：含逗号/引号/换行时加引号并转义内部引号 */
    private static String csvField(String s) {
        if (s == null) {
            return "";
        }
        boolean needQuote = s.indexOf(',') >= 0
                || s.indexOf('"') >= 0
                || s.indexOf('\n') >= 0
                || s.indexOf('\r') >= 0;
        String v = s.replace("\"", "\"\"");
        return needQuote ? "\"" + v + "\"" : v;
    }
}
