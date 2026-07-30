package algoapi.exception;

import algoapi.model.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常兜底处理器
 * 统一捕获异常，返回标准化错误结构，避免异常堆栈直接暴露给用户
 *
 * CR 修复（2026-07-30）：
 * - I1: 返回体包裹 ApiResult 统一响应体 {code,message,data}
 * - N2: 兜底 Exception 加 slf4j 日志，记录完整堆栈
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 非法参数（如 export 非法 type）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(400, e.getMessage()));
    }

    /**
     * 缺少必填参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(400, "缺少必填参数: " + e.getParameterName()));
    }

    /**
     * 兜底：所有未捕获异常
     * N2: 记录完整堆栈日志，便于生产排查
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult> handleException(Exception e) {
        log.error("未捕获异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(500, "服务异常，请稍后重试"));
    }
}
