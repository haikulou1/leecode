package com.antdigital.staff.common.model;

import java.io.Serializable;

/**
 * 通用出参结构：{@code { code, msg, data }}，code=0 表示成功。
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 结果码，0 表示成功。 */
    private Integer code;

    /** 错误码，成功时为空；失败时携带 STAFF_xxx。 */
    private String errorCode;

    /** 提示信息。 */
    private String msg;

    /** 用户提示。 */
    private String userTip;

    /** 业务数据。 */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "SUCCESS", data);
    }

    public static <T> ApiResponse<T> error(String errorCode, String msg, String userTip) {
        ApiResponse<T> response = new ApiResponse<>(1, msg, null);
        response.setErrorCode(errorCode);
        response.setUserTip(userTip);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUserTip() {
        return userTip;
    }

    public void setUserTip(String userTip) {
        this.userTip = userTip;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResponse{"
                + "code=" + code
                + ", errorCode='" + errorCode + '\''
                + ", msg='" + msg + '\''
                + ", data=" + data
                + '}';
    }
}