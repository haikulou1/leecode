package com.antdigital.staff.common.enums;

/**
 * 人员看板错误码枚举（STAFF 前缀，按模块分段）。
 */
public enum StaffErrorCode {

    // ---- 通用 ----
    PARAM_INVALID("STAFF_001", "参数校验失败"),
    NOT_FOUND("STAFF_002", "资源不存在"),

    // ---- 员工信息模块 ----
    EMP_PARAM_INVALID("STAFF_EMP_001", "参数校验失败（缺 empNo/name 等）"),
    EMP_NO_EXISTS("STAFF_EMP_002", "工号已存在"),
    EMP_NO_NOT_EXISTS("STAFF_EMP_003", "工号不存在"),
    EMP_NOT_FOUND("STAFF_EMP_004", "员工不存在或已删除"),

    // ---- 导入模块 ----
    IMP_FILE_INVALID("STAFF_IMP_001", "文件为空/格式不支持"),
    IMP_FILE_TOO_LARGE("STAFF_IMP_002", "文件大小超限"),
    IMP_PARSE_FAILED("STAFF_IMP_003", "文件解析失败/模板不匹配"),
    IMP_TASK_NOT_FOUND("STAFF_IMP_004", "任务不存在"),

    // ---- 成本预算模块 ----
    COST_EMP_NOT_FOUND("STAFF_COST_001", "员工不存在"),
    COST_AMOUNT_INVALID("STAFF_COST_002", "金额非法≤0 或超精度"),

    // ---- 白名单模块 ----
    WL_NOT_IN_WHITELIST("STAFF_WL_001", "该工号未在白名单内，禁止录入"),
    WL_EMP_EXISTS("STAFF_WL_002", "该工号已在白名单"),

    // ---- 系统 ----
    SYSTEM_ERROR("STAFF_SYS_001", "系统内部错误");

    private final String code;
    private final String msg;

    StaffErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}