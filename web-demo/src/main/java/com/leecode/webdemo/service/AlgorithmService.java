package com.leecode.webdemo.service;

import com.leecode.webdemo.dto.BubbleSortResult;
import com.leecode.webdemo.dto.HashResult;

import java.util.List;

/**
 * 算法服务接口，封装 HelloWorld、哈希、冒泡排序核心能力。
 *
 * @author DTCoder
 */
public interface AlgorithmService {

    /**
     * HelloWorld，返回固定问候字符串。
     *
     * @return 问候字符串
     */
    String helloWorld();

    /**
     * 哈希计算。
     *
     * @param input     输入字符串
     * @param algorithm 算法名称（MD5 / SHA_256），null 默认 SHA_256
     * @return 哈希结果
     */
    HashResult hash(String input, String algorithm);

    /**
     * 冒泡排序。
     *
     * @param numbers 待排序数组
     * @param order   排序方向（ASC / DESC），null 默认 ASC
     * @return 排序结果
     */
    BubbleSortResult bubbleSort(List<Integer> numbers, String order);
}
