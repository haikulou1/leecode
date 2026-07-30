package com.demo.helloworld.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果封装
 * 出参结构：{code, msg, data}
 *
 * @param <T> 业务数据类型
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 结果 code，成功为 "OK" */
    private String code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    /**
     * 构建成功结果
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.OK.getCode(), ResultCode.OK.getMsg(), data);
    }

    /**
     * 构建成功结果（无数据）
     *
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.OK.getCode(), ResultCode.OK.getMsg(), null);
    }

    /**
     * 构建失败结果
     *
     * @param resultCode 错误码枚举
     * @param <T>         数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 构建失败结果（自定义提示信息）
     *
     * @param resultCode 错误码枚举
     * @param msg        自定义提示信息
     * @param <T>        数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> fail(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }
}
