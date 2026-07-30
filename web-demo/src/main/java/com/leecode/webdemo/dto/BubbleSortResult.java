package com.leecode.webdemo.dto;

import lombok.Data;

import java.util.List;

/**
 * 冒泡排序结果。
 *
 * @author DTCoder
 */
@Data
public class BubbleSortResult {

    /** 原始输入 */
    private List<Integer> input;

    /** 排序结果 */
    private List<Integer> sorted;

    /** 实际排序方向 */
    private String order;

    /** 是否截断兜底 */
    private Boolean truncated;
}
