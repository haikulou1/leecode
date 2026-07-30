package server.model;

/**
 * 统一响应体 {code, msg, data}，零依赖手写 JSON 序列化（不引入 Jackson）。
 * dataJson 为预序列化的 data JSON 片段，由各 handler 拼接后传入。
 */
public class ApiResponse {

    private final int code;
    private final String msg;
    private final String dataJson;

    private ApiResponse(int code, String msg, String dataJson) {
        this.code = code;
        this.msg = msg;
        this.dataJson = dataJson;
    }

    /** 成功响应，dataJson 为预序列化 JSON 片段 */
    public static ApiResponse ok(String dataJson) {
        return new ApiResponse(200, "success", dataJson);
    }

    /** 错误响应，data 为 null */
    public static ApiResponse error(int code, String msg) {
        return new ApiResponse(code, msg, null);
    }

    /** 序列化为完整 JSON 字符串 */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"code\":").append(code);
        sb.append(",\"msg\":\"").append(escape(msg)).append("\"");
        if (dataJson != null) {
            sb.append(",\"data\":").append(dataJson);
        } else {
            sb.append(",\"data\":null");
        }
        sb.append("}");
        return sb.toString();
    }

    /** JSON 字符串值转义 */
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
