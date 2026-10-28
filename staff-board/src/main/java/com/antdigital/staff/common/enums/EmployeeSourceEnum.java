package com.antdigital.staff.common.enums;

/**
 * 员工数据来源枚举。
 */
public enum EmployeeSourceEnum {

    MANUAL("MANUAL", "手动录入"),
    IMPORT("IMPORT", "批量导入");

    private final String code;
    private final String desc;

    EmployeeSourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}