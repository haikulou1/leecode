package com.leecode.webdemo.common;

import lombok.Data;

/**
 * 统一响应结构。
 *
 * @param <T> data 范型
 * @author DTCoder
 */
@Data
public class ApiResponse<T> {

    /** 状态码 OK / ERROR */
    private String code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    /**
     * 构建成功响应。
     *
     * @param data 业务数据
     * @param <T>  范型
     * @return 统一响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResultCode.OK.name());
        response.setMsg("SUCCESS");
        response.setData(data);
        return response;
    }

    /**
     * 构建成功响应，自定义提示信息。
     *
     * @param data 业务数据
     * @param msg  提示信息
     * @param <T>  范型
     * @return 统一响应
     */
    public static <T> ApiResponse<T> success(T data, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResultCode.OK.name());
        response.setMsg(msg);
        response.setData(data);
        return response;
    }

    /**
     * 构建失败响应。
     *
     * @param msg 错误信息
     * @param <T> 范型
     * @return 统一响应
     */
    public static <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResultCode.ERROR.name());
        response.setMsg(msg);
        response.setData(null);
        return response;
    }

    /**
     * 构建失败响应，带错误码前缀。
     *
     * @param errorCode 错误码
     * @param msg       错误描述
     * @param <T>       范型
     * @return 统一响应
     */
    public static <T> ApiResponse<T> error(String errorCode, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResultCode.ERROR.name());
        response.setMsg(errorCode + ": " + msg);
        response.setData(null);
        return response;
    }
}
