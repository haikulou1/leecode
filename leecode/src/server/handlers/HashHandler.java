package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ApiServer;
import server.model.ApiResponse;

import java.security.MessageDigest;
import java.util.Map;

/**
 * GET /api/hash?input=xxx&algo=sha256|md5
 * 出参：{"code":200,"msg":"success","data":{"input":"abc","algo":"sha256","hash":"..."}}
 * 错误：input 为空返回 code=400, msg="input required"
 */
public class HashHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        if (ApiServer.handleOptions(exchange)) {
            return;
        }
        try {
            Map<String, String> params = ApiServer.parseQuery(exchange.getRequestURI());
            String input = params.getOrDefault("input", "");
            String algo = params.getOrDefault("algo", "sha256");
            if (input.isEmpty()) {
                ApiServer.writeJson(exchange, ApiResponse.error(400, "input required"), 400);
                return;
            }
            String hash = computeHash(input, algo);
            String dataJson = "{\"input\":\"" + ApiResponse.escape(input) + "\""
                    + ",\"algo\":\"" + ApiResponse.escape(algo) + "\""
                    + ",\"hash\":\"" + ApiResponse.escape(hash) + "\"}";
            ApiServer.writeJson(exchange, ApiResponse.ok(dataJson), 200);
        } catch (Exception e) {
            ApiServer.writeError(exchange, 500, "internal error: " + e.getMessage());
        }
    }

    /**
     * 业务逻辑：计算哈希，供 ExportHandler 复用。
     * algo 支持 sha256（默认）/ md5，使用 JDK 内置 MessageDigest，零依赖。
     */
    public static String computeHash(String input, String algo) throws Exception {
        String algorithm;
        if ("md5".equalsIgnoreCase(algo)) {
            algorithm = "MD5";
        } else {
            algorithm = "SHA-256";
        }
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes("UTF-8"));
        return toHex(digest);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
