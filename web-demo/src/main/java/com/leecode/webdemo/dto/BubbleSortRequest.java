package com.leecode.webdemo.dto;

import lombok.Data;

import java.util.List;

/**
 * 冒泡排序请求。
 * 冒泡排序请求。
 *
 * @author DTCoder
 */
@Data
public class BubbleSortRequest {

    /** 待排序整型数组（必填） */
    private List<Integer> numbers;

    /** 排序方向（可选，默认 ASC） */
    private String order;
}
