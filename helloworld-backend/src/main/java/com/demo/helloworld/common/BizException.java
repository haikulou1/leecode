package com.demo.helloworld.common;

import lombok.Getter;

/**
 * 业务异常
 * 用于在 Service 层抛出可预期的业务错误，由全局异常处理器统一捕获
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final ResultCode resultCode;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
