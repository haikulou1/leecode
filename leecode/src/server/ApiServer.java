package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import server.handlers.BubbleSortHandler;
import server.handlers.ExportHandler;
import server.handlers.HashHandler;
import server.handlers.HelloWorldHandler;
import server.model.ApiResponse;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServer 启动入口 + 路由注册 + CORS + query 解析 + JSON 工具。
 * 使用 JDK 内置 com.sun.net.httpserver.HttpServer，零外部依赖。
 *
 * 启动（在 leecode/src 目录下执行）：
 *   javac -d ../out server/*.java server/handlers/*.java server/model/*.java
 *   java -cp ../out server.ApiServer
 * 端口：-Dport=9090 覆盖，默认 8080，监听 0.0.0.0
 */
public class ApiServer {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api/helloworld", new HelloWorldHandler());
        server.createContext("/api/hash", new HashHandler());
        server.createContext("/api/bubble-sort", new BubbleSortHandler());
        server.createContext("/api/export", new ExportHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("ApiServer started on port " + port);
        System.out.println("  GET /api/helloworld");
        System.out.println("  GET /api/hash?input=xxx&algo=sha256|md5");
        System.out.println("  GET /api/bubble-sort");
        System.out.println("  GET /api/export?type=helloworld|hash|bubble-sort&format=csv");
    }

    /** 统一 CORS 响应头 */
    public static void applyCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    /** 解析 query 参数，URL 解码 */
    public static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx < 0) {
                key = pair;
                value = "";
            } else {
                key = pair.substring(0, idx);
                value = pair.substring(idx + 1);
            }
            try {
                params.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
            } catch (Exception e) {
                params.put(key, value);
            }
        }
        return params;
    }

    /** 写 JSON 响应（含 CORS + Content-Type） */
    public static void writeJson(HttpExchange exchange, ApiResponse resp, int status) {
        try {
            applyCors(exchange);
            byte[] body = resp.toJson().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        } catch (Exception ignored) {
            // ignore write failure
        }
    }

    /** 写错误 JSON */
    public static void writeError(HttpExchange exchange, int status, String msg) {
        writeJson(exchange, ApiResponse.error(status, msg), status);
    }

    /** OPTIONS 预检处理，返回 true 表示已处理（handler 应直接 return） */
    public static boolean handleOptions(HttpExchange exchange) {
        applyCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                exchange.sendResponseHeaders(204, -1);
            } catch (Exception ignored) {
                // ignore
            }
            return true;
        }
        return false;
    }
}
