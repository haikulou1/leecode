import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 排序算法接口处理器（默认候选A，第三个接口）：
 * GET /sort?algorithm=quicksort&input=3,1,2 → "1,2,3"
 *
 * <p>支持算法：bubble / quicksort / selection。
 */
public class SortHandler implements HttpHandler {

    /** input 参数长度上限（防 DoS/OOM）。 */
    private static final int MAX_INPUT_LENGTH = 1024;

    /** input 解析后元素数量上限（防超大数组 OOM）。 */
    private static final int MAX_ELEMENT_COUNT = 1000;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 405, "Method Not Allowed: only GET is supported");
            return;
        }
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());

        String algorithm = params.get("algorithm");
        String input = params.get("input");

        if (algorithm == null || algorithm.isEmpty()) {
            write(exchange, 400, "Missing required parameter: algorithm");
            return;
        }
        if (input == null) {
            write(exchange, 400, "Missing required parameter: input");
            return;
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            write(exchange, 400, "Input too long: max " + MAX_INPUT_LENGTH + " chars");
            return;
        }
        if (!SortUtil.isSupported(algorithm)) {
            write(exchange, 400, "Unsupported algorithm: " + algorithm
                    + " (supported: bubble, quicksort, selection)");
            return;
        }

        int[] arr;
        try {
            arr = parseInput(input);
        } catch (NumberFormatException e) {
            System.err.println("[WARN] [SortHandler] parse failed: inputLen=" + input.length()
                    + ", error=" + e.getMessage());
            write(exchange, 400, "Invalid input: expect comma-separated integers, e.g. 3,1,2");
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("[WARN] [SortHandler] parse rejected: inputLen=" + input.length()
                    + ", error=" + e.getMessage());
            write(exchange, 400, e.getMessage());
            return;
        }

        int[] sorted = SortUtil.sort(algorithm, arr);
        write(exchange, 200, join(sorted));
    }

    /** 解析逗号分隔整数为 int 数组。 */
    private static int[] parseInput(String input) {
        if (input.isEmpty()) {
            return new int[0];
        }
        String[] parts = input.split(",");
        if (parts.length > MAX_ELEMENT_COUNT) {
            throw new IllegalArgumentException(
                    "Too many elements: max " + MAX_ELEMENT_COUNT + ", got " + parts.length);
        }
        int[] arr = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Integer.parseInt(parts[i].trim());
        }
        return arr;
    }

    /** 数组以逗号连接为字符串。 */
    private static String join(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx < 0) {
                key = URLDecoder.decode(pair, StandardCharsets.UTF_8);
                value = "";
            } else {
                key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            }
            params.putIfAbsent(key, value);
        }
        return params;
    }

    private static void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
