package com.antdigital.staff.common.enums;

/**
 * 成本类型枚举。
 */
public enum CostTypeEnum {

    SALARY("SALARY", "工资"),
    SOCIAL_INSURANCE("SOCIAL_INSURANCE", "社保"),
    BONUS("BONUS", "奖金"),
    ALLOCATION("ALLOCATION", "部门分摊"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    CostTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 成本类型采用字符串枚举，仅做格式校验，未知类型不拒绝（便于后续扩展）。 */
    public static boolean isValid(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (CostTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}