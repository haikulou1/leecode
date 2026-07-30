package com.leecode.webdemo.enums;

/**
 * 导出格式枚举。
 *
 * @author DTCoder
 */
public enum ExportFormatEnum {

    /** CSV 格式 */
    CSV,
    /** JSON 格式 */
    JSON;

    /**
     * 根据名称解析枚举，不区分大小写，不匹配返回 null。
     *
     * @param name 枚举名称
     * @return 枚举值，不匹配时返回 null
     */
    public static ExportFormatEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ExportFormatEnum format : values()) {
            if (format.name().equalsIgnoreCase(name.trim())) {
                return format;
            }
        }
        return null;
    }
}
