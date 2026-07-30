package com.demo.helloworld.enums;

import lombok.Getter;

/**
 * 是否删除枚举
 * 关联字段：hello_message.is_deleted / contact_record.is_deleted
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public enum IsDeletedEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    /** 状态值 */
    private final Integer value;

    /** 描述 */
    private final String desc;

    IsDeletedEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
