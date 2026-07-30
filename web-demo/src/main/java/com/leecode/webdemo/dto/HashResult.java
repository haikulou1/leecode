package com.leecode.webdemo.dto;

import lombok.Data;

/**
 * 哈希计算结果。
 *
 * @author DTCoder
 */
@Data
public class HashResult {

    /** 原始输入 */
    private String input;

    /** 实际使用的算法 */
    private String algorithm;

    /** 哈希值（十六进制） */
    private String hashValue;
}
