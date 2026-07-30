package com.demo.helloworld.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 联系记录处理状态枚举
 * 关联字段：contact_record.status
 * 状态流转：PENDING -> RESOLVED -> CLOSED / PENDING -> CLOSED
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public enum ContactStatusEnum {

    PENDING("PENDING", "待处理"),
    RESOLVED("RESOLVED", "已处理"),
    CLOSED("CLOSED", "已关闭");

    /** 状态码 */
    private final String code;

    /** 描述 */
    private final String desc;

    ContactStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return 枚举值，不存在返回 null
     */
    public static ContactStatusEnum of(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
