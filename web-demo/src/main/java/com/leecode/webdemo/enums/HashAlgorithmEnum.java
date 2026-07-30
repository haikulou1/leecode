package com.leecode.webdemo.enums;

/**
 * 哈希算法枚举。
 *
 * @author DTCoder
 */
public enum HashAlgorithmEnum {

    /** MD5 */
    MD5("MD5"),
    /** SHA-256 */
    SHA_256("SHA-256");

    private final String digestName;

    HashAlgorithmEnum(String digestName) {
        this.digestName = digestName;
    }

    /**
     * 获取 MessageDigest 使用的算法名称。
     *
     * @return 算法名称
     */
    public String getDigestName() {
        return digestName;
    }

    /**
     * 根据名称解析枚举，不区分大小写，不匹配返回 null。
     *
     * @param name 枚举名称
     * @return 枚举值，不匹配时返回 null
     */
    public static HashAlgorithmEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String normalized = name.trim().toUpperCase().replace("-", "_");
        for (HashAlgorithmEnum algorithm : values()) {
            if (algorithm.name().equalsIgnoreCase(normalized)) {
                return algorithm;
            }
        }
        return null;
    }
}
