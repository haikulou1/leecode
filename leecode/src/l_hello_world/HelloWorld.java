import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * HelloWorld 后端服务入口，启动内嵌 HTTP 服务并对外提供 /hello 接口，
 * 供前端 haikulou1.github.io 的 hello.html 页面调用。
 *
 * @author dtcoder
 * @date 2026/07/29
 */
public class HelloWorld {

    /**
     * HTTP 服务监听端口
     */
    private static final int SERVER_PORT = 8080;

    /**
     * 对外暴露的接口路径
     */
    private static final String HELLO_PATH = "/hello";

    /**
     * 响应体 JSON 内容，前端按 message 字段解析
     */
    private static final String HELLO_RESPONSE_BODY =
            "{\"message\":\"Hello, World!\",\"source\":\"leecode\"}";

    /**
     * 程序入口，启动 HTTP 服务并阻塞等待前端请求。
     *
     * @param args 启动参数，当前未使用
     * @throws IOException 当 HTTP 服务绑定端口失败时抛出
     */
    public static void main(String[] args) throws IOException {
        // 创建 HTTP 服务并绑定本地端口
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        // 注册 /hello 接口的处理器
        server.createContext(HELLO_PATH, new HelloHandler());
        server.start();

        System.out.println("HelloWorld 服务已启动，监听端口：" + SERVER_PORT);
        System.out.println("接口地址：http://localhost:" + SERVER_PORT + HELLO_PATH);
    }

    /**
     * /hello 接口处理器，返回固定 JSON 响应，并设置跨域头以兼容 GitHub Pages 前端。
     *
     * @author dtcoder
     * @date 2026/07/29
     */
    static class HelloHandler implements HttpHandler {

        /**
         * 处理前端请求，写入 JSON 响应并返回 200。
         *
         * @param exchange HTTP 交互上下文
         * @throws IOException 写出响应体失败时抛出
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 设置跨域响应头，允许 GitHub Pages 前端跨域访问
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

            // 写出响应体
            byte[] responseBytes = HELLO_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
