package algoapi.model;

/**
 * 统一响应体，包裹 {code, message, data} 结构。
 * 对应 clarify.md §4.3 跨库接口契约。
 */
public class ApiResult {

    private final int code;
    private final String message;
    private final Object data;

    private ApiResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应，code=0, message="ok"
     */
    public static ApiResult ok(Object data) {
        return new ApiResult(0, "ok", data);
    }

    /**
     * 失败响应，code 非 0
     */
    public static ApiResult fail(int code, String message) {
        return new ApiResult(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
