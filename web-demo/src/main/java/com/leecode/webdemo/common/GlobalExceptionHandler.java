package com.leecode.webdemo.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，捕获业务异常和未知异常，返回兜底响应。
 *
 * <p>兜底策略：任意未捕获异常返回 {@code {code:ERROR, msg:"服务异常，请稍后重试", data:null}}，HTTP 200。</p>
 *
 * @author DTCoder
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param ex 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Object>> handleBizException(BizException ex) {
        log.warn("业务异常: code={}, msg={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * 处理参数校验异常。
     *
     * @param ex 参数校验异常
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", msg);
        return ResponseEntity.ok(ApiResponse.error(msg));
    }

    /**
     * 处理未知异常，返回兜底响应。
     *
     * @param ex 未知异常
     * @return 兜底错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("未知异常，触发兜底: ", ex);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.error("服务异常，请稍后重试"));
    }
}
