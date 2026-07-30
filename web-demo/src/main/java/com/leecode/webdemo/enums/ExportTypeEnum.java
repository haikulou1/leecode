package com.leecode.webdemo.enums;

/**
 * 导出类型枚举。
 *
 * @author DTCoder
 */
public enum ExportTypeEnum {

    /** HelloWorld 结果导出 */
    HELLO_WORLD,
    /** 哈希结果导出 */
    HASH,
    /** 冒泡排序结果导出 */
    BUBBLE_SORT;

    /**
     * 根据名称解析枚举，不区分大小写，不匹配返回 null。
     *
     * @param name 枚举名称
     * @return 枚举值，不匹配时返回 null
     */
    public static ExportTypeEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ExportTypeEnum type : values()) {
            if (type.name().equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        return null;
    }
}
