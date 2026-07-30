package com.leecode.webdemo.common;

import lombok.Getter;

/**
 * 业务异常，携带错误码。
 *
 * @author DTCoder
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final String errorCode;

    /**
     * 构造业务异常。
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
