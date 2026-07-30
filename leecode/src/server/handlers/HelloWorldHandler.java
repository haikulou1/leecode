package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ApiServer;
import server.model.ApiResponse;

/**
 * GET /api/helloworld
 * 出参：{"code":200,"msg":"success","data":{"message":"Hello, World!"}}
 */
public class HelloWorldHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        if (ApiServer.handleOptions(exchange)) {
            return;
        }
        try {
            String dataJson = "{\"message\":\"" + ApiResponse.escape(getMessage()) + "\"}";
            ApiServer.writeJson(exchange, ApiResponse.ok(dataJson), 200);
        } catch (Exception e) {
            ApiServer.writeError(exchange, 500, "internal error: " + e.getMessage());
        }
    }

    /** 业务逻辑，供 ExportHandler 复用 */
    public static String getMessage() {
        return "Hello, World!";
    }
}
