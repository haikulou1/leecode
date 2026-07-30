package com.leecode.webdemo.dto;

import lombok.Data;

/**
 * 哈希计算请求。
 *
 * @author DTCoder
 */
@Data
public class HashRequest {

    /** 哈希输入字符串（必填） */
    private String input;

    /** 哈希算法（可选，默认 SHA_256） */
    private String algorithm;
}
