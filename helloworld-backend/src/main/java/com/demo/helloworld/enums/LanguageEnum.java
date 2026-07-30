package com.demo.helloworld.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 语言枚举
 * 关联字段：hello_message.language
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public enum LanguageEnum {

    ZH_CN("zh_CN", "简体中文"),
    EN_US("en_US", "英文");

    /** 语言标识 */
    private final String code;

    /** 描述 */
    private final String desc;

    LanguageEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据语言标识获取枚举
     *
     * @param code 语言标识
     * @return 枚举值，不存在返回 null
     */
    public static LanguageEnum of(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断语言标识是否合法
     *
     * @param code 语言标识
     * @return true=合法
     */
    public static boolean isValid(String code) {
        return of(code) != null;
    }
}
