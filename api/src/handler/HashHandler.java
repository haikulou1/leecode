import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 哈希算法接口处理器：GET /hash?algorithm=sha256&input=abc → 十六进制摘要。
 *
 * <p>支持算法：md5 / sha1 / sha256 / sha512。
 */
public class HashHandler implements HttpHandler {

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
        if (!HashUtil.isSupported(algorithm)) {
            write(exchange, 400, "Unsupported algorithm: " + algorithm
                    + " (supported: md5, sha1, sha256, sha512)");
            return;
        }
        try {
            String digest = HashUtil.digest(algorithm, input);
            write(exchange, 200, digest);
        } catch (IllegalArgumentException e) {
            write(exchange, 400, e.getMessage());
        }
    }

    /** 解析 URL 查询串为参数表（值做 URL 解码）。 */
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
