import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 启动入口：基于 JDK 原生 {@link HttpServer} 注册三个接口路由并启动服务。
 *
 * <p>默认端口 8080，可通过启动参数 {@code args[0]} 覆盖。
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[WARN] [Main] Invalid port arg: args[0]=" + args[0]
                        + ", exception=" + e.getClass().getSimpleName()
                        + ", fallback=8080");
            }
        }

        final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/helloworld", new HelloWorldHandler());
        server.createContext("/hash", new HashHandler());
        server.createContext("/sort", new SortHandler());
        // 未知路径兜底：404
        server.createContext("/", new NotFoundHandler());
        server.setExecutor(null); // 默认线程池

        // 生产建议：JVM 退出时优雅关闭 HttpServer，避免端口/资源残留
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("[INFO] [Main] Shutdown hook: stopping HttpServer");
            server.stop(0);
        }));

        server.start();

        System.out.println("API server started on port " + port);
        System.out.println("  GET /helloworld            -> Hello, World!");
        System.out.println("  GET /hash?algorithm=..&input=.. -> hex digest");
        System.out.println("  GET /sort?algorithm=..&input=..  -> sorted array");
    }

    /** 未知路径返回 404。 */
    static class NotFoundHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] data = "Not Found".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }
}
