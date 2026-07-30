package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

/**
 * 入口类：启动 HTTP 服务，绑定 127.0.0.1:8080。
 * 使用 JDK 自带 com.sun.net.httpserver.HttpServer，零外部依赖。
 */
public class Main {

    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/", new RouterHandler());
        server.setExecutor(null); // 默认执行器
        server.start();
        System.out.println("Server started on 127.0.0.1:8080");
    }
}
