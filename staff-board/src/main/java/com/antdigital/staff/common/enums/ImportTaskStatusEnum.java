package com.antdigital.staff.common.enums;

/**
 * 导入任务状态枚举。
 */
public enum ImportTaskStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    PARTIAL(3, "部分成功"),
    FAILED(4, "失败");

    private final int code;
    private final String desc;

    ImportTaskStatusEnum(int code, String desc) {
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