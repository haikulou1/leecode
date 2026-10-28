package com.antdigital.staff.common.enums;

/**
 * 白名单状态枚举。
 */
public enum WhitelistStatusEnum {

    ENABLED(1, "启用"),
    DISABLED(0, "停用");

    private final int code;
    private final String desc;

    WhitelistStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}