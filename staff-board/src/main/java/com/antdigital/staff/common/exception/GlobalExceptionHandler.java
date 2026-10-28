package com.antdigital.staff.common.exception;

import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：将业务异常与参数校验异常转换为 {@link ApiResponse}。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        if (LOG.isInfoEnabled()) {
            LOG.info("业务异常 errorCode:{}, msg:{}, userTip:{}", ex.getErrorCode(), ex.getMessage(), ex.getUserTip());
        }
        return ApiResponse.<Void>error(ex.getErrorCode(), ex.getMessage(), ex.getUserTip());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidationException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String detail = fieldError != null ? fieldError.getField() + " " + fieldError.getDefaultMessage() : "参数校验失败";
        return ApiResponse.<Void>error(StaffErrorCode.PARAM_INVALID.getCode(), "参数校验失败", detail);
    }

    @ExceptionHandler(Throwable.class)
    public ApiResponse<Void> handleThrowable(Throwable ex) {
        LOG.error("系统异常: ", ex);
        return ApiResponse.<Void>error(StaffErrorCode.SYSTEM_ERROR.getCode(), StaffErrorCode.SYSTEM_ERROR.getMsg(),
                StaffErrorCode.SYSTEM_ERROR.getMsg());
    }
}