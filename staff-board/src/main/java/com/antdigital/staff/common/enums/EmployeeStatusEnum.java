package com.antdigital.staff.common.enums;

/**
 * 员工状态枚举。
 */
public enum EmployeeStatusEnum {

    NORMAL(1, "在职/正常"),
    LEAVE(0, "离职/停用");

    private final int code;
    private final String desc;

    EmployeeStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 按编码查找；非法编码返回 null（由调用方决定策略）。 */
    public static EmployeeStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EmployeeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }
}