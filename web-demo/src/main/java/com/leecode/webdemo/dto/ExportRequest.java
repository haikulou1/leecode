package com.leecode.webdemo.dto;

import lombok.Data;

import java.util.List;

/**
 * 导出请求，根据 type 携带对应算法参数。
 *
 * @author DTCoder
 */
@Data
public class ExportRequest {

    /** 导出类型 HELLO_WORLD / HASH / BUBBLE_SORT（必填） */
    private String type;

    /** 导出格式 CSV / JSON（可选，默认 CSV） */
    private String format;

    /** 哈希输入（type=HASH 时必填） */
    private String hashInput;

    /** 哈希算法（type=HASH 时可选） */
    private String hashAlgorithm;

    /** 排序数组（type=BUBBLE_SORT 时必填） */
    private List<Integer> sortNumbers;

    /** 排序方向（type=BUBBLE_SORT 时可选） */
    private String sortOrder;
}
