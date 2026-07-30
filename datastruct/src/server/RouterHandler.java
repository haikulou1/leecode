package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import service.BubbleSortService;
import service.ExportService;
import service.HashService;
import service.HelloWorldService;
import util.JsonUtil;

/**
 * 路由处理器：解析路径与 query，分发到对应 service。
 * 统一设置 CORS 头；OPTIONS 预检返回 204；错误统一返回 4xx + JSON。
 */
public class RouterHandler implements HttpHandler {

    private static final String CORS_ORIGIN = "*";
    private static final String CORS_METHODS = "GET, OPTIONS";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String CSV_CONTENT_TYPE = "text/csv; charset=utf-8";

    private final HelloWorldService helloService = new HelloWorldService();
    private final HashService hashService = new HashService();
    private final BubbleSortService bubbleService = new BubbleSortService();
    private final ExportService exportService = new ExportService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // 统一 CORS 头
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", CORS_ORIGIN);
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", CORS_METHODS);

            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();

            // OPTIONS 预检：204 + CORS
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(method)) {
                sendError(exchange, 405, "method not allowed");
                return;
            }

            Map<String, String> query = parseQuery(uri.getQuery());

            if ("/api/helloworld".equals(path)) {
                sendJson(exchange, 200, helloService.hello());
                return;
            }

            if ("/api/hash".equals(path)) {
                String input = query.get("input");
                if (input == null || input.isEmpty()) {
                    sendError(exchange, 400, "input is required");
                    return;
                }
                if (input.length() > 1024) {
                    sendError(exchange, 400, "input is required");
                    return;
                }
                sendJson(exchange, 200, hashService.hash(input));
                return;
            }

            if ("/api/bubble".equals(path)) {
                String data = query.get("data");
                if (data == null || data.isEmpty()) {
                    sendError(exchange, 400, "data format invalid");
                    return;
                }
                try {
                    sendJson(exchange, 200, bubbleService.sort(data));
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, "data format invalid");
                }
                return;
            }

            if ("/api/export".equals(path)) {
                String tab = query.get("tab");
                if (tab == null || tab.isEmpty()) {
                    sendError(exchange, 400, "invalid tab");
                    return;
                }
                try {
                    byte[] csv = exportService.export(tab);
                    String filename = tab + ".csv";
                    exchange.getResponseHeaders().set("Content-Type", CSV_CONTENT_TYPE);
                    exchange.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=\"" + filename + "\"");
                    exchange.sendResponseHeaders(200, csv.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(csv);
                    os.close();
                } catch (IllegalArgumentException e) {
                    sendError(exchange, 400, "invalid tab");
                }
                return;
            }

            // 未知路径
            sendError(exchange, 404, "not found");
        } catch (Exception e) {
            sendError(exchange, 500, "internal server error");
        }
    }

    /**
     * 解析 query 字符串为键值对（简单实现，UTF-8 解码）。
     */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<String, String>();
        if (query == null || query.isEmpty()) {
            return map;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx < 0) {
                key = urlDecode(pair);
                value = "";
            } else {
                key = urlDecode(pair.substring(0, idx));
                value = urlDecode(pair.substring(idx + 1));
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * 简易 URL 解码（处理 %xx 与 +）。
     */
    private String urlDecode(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '+') {
                sb.append(' ');
                i++;
            } else if (c == '%' && i + 2 < s.length()) {
                try {
                    int hex = Integer.parseInt(s.substring(i + 1, i + 3), 16);
                    sb.append((char) hex);
                    i += 3;
                } catch (NumberFormatException e) {
                    sb.append(c);
                    i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 发送 JSON 成功响应。
     */
    private void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.sendResponseHeaders(status, data.length);
        OutputStream os = exchange.getResponseBody();
        os.write(data);
        os.close();
    }

    /**
     * 发送错误响应（JSON {"error":"<message>"}，4xx）。
     */
    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String body = "{\"error\":\"" + JsonUtil.escape(message) + "\"}";
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_CONTENT_TYPE);
        exchange.sendResponseHeaders(status, data.length);
        OutputStream os = exchange.getResponseBody();
        os.write(data);
        os.close();
    }
}
