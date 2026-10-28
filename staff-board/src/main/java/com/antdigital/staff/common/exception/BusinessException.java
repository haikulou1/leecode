package com.antdigital.staff.common.exception;

import com.antdigital.staff.common.enums.StaffErrorCode;

/**
 * 业务异常：携带模块错误码，由全局异常处理器统一转换为 HTTP 响应。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String userTip;

    public BusinessException(String errorCode, String msg) {
        this(errorCode, msg, msg);
    }

    public BusinessException(String errorCode, String msg, String userTip) {
        super(msg);
        this.errorCode = errorCode;
        this.userTip = userTip;
    }

    public BusinessException(StaffErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMsg());
    }

    public BusinessException(StaffErrorCode errorCode, String userTip) {
        this(errorCode.getCode(), errorCode.getMsg(), userTip);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserTip() {
        return userTip;
    }
}