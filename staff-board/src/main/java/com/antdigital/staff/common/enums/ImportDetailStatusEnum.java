package com.antdigital.staff.common.enums;

/**
 * 导入明细处理状态枚举。
 */
public enum ImportDetailStatusEnum {

    SUCCESS(1, "成功"),
    FAIL(0, "失败");

    private final int code;
    private final String desc;

    ImportDetailStatusEnum(int code, String desc) {
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