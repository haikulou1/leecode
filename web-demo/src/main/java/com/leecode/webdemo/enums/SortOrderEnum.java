package com.leecode.webdemo.enums;

/**
 * 排序方向枚举。
 *
 * @author DTCoder
 */
public enum SortOrderEnum {

    /** 升序 */
    ASC,
    /** 降序 */
    DESC;

    /**
     * 根据名称解析枚举，不区分大小写，不匹配返回 null。
     *
     * @param name 枚举名称
     * @return 枚举值，不匹配时返回 null
     */
    public static SortOrderEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (SortOrderEnum order : values()) {
            if (order.name().equalsIgnoreCase(name.trim())) {
                return order;
            }
        }
        return null;
    }
}
