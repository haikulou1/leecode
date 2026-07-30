package com.demo.helloworld.common;

import lombok.Getter;

/**
 * 统一错误码枚举
 * 命名规范：模块名_序号，如 HELLO_001
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public enum ResultCode {

    /** 通用成功 */
    OK("OK", "SUCCESS"),

    /** 通用系统错误 */
    SYSTEM_ERROR("SYSTEM_ERROR", "系统繁忙，请稍后再试"),

    /** 参数校验错误 */
    PARAM_ERROR("PARAM_ERROR", "参数校验失败"),

    /** 欢迎语模块错误码 */
    HELLO_001("HELLO_001", "欢迎语未配置"),
    HELLO_002("HELLO_002", "语言参数不合法"),

    /** 关于模块错误码 */
    ABOUT_001("ABOUT_001", "关于信息未配置"),

    /** 联系模块错误码 */
    CONTACT_001("CONTACT_001", "联系信息未配置"),
    CONTACT_002("CONTACT_002", "姓名为空或超长"),
    CONTACT_003("CONTACT_003", "邮箱格式不合法"),
    CONTACT_004("CONTACT_004", "留言内容为空或超长"),

    /** 运维模块错误码 */
    CONFIG_001("CONFIG_001", "配置加载失败");

    /** 结果 code */
    private final String code;

    /** 提示信息 */
    private final String msg;

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
