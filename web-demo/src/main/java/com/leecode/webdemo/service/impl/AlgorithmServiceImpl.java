package com.leecode.webdemo.service.impl;

import com.leecode.webdemo.common.AlgorithmConstants;
import com.leecode.webdemo.common.BizException;
import com.leecode.webdemo.dto.BubbleSortResult;
import com.leecode.webdemo.dto.HashResult;
import com.leecode.webdemo.enums.HashAlgorithmEnum;
import com.leecode.webdemo.enums.SortOrderEnum;
import com.leecode.webdemo.service.AlgorithmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 算法服务实现，含完整兜底降级逻辑。
 *
 * @author DTCoder
 */
@Slf4j
@Service
public class AlgorithmServiceImpl implements AlgorithmService {

    @Override
    public String helloWorld() {
        try {
            return AlgorithmConstants.HELLO_WORLD_MESSAGE;
        } catch (Exception e) {
            // 兜底：Service 异常时返回固定值
            log.error("HelloWorld 服务异常，触发兜底: ", e);
            return AlgorithmConstants.HELLO_WORLD_MESSAGE;
        }
    }

    @Override
    public HashResult hash(String input, String algorithm) {
        // R01: input 空白 → ALGORITHM_002
        if (input == null || input.trim().isEmpty()) {
            throw new BizException(AlgorithmConstants.ALGORITHM_002, "哈希输入为空");
        }

        // R02: algorithm 空 → 默认 SHA_256
        HashAlgorithmEnum algoEnum = HashAlgorithmEnum.fromName(algorithm);
        if (algoEnum == null && algorithm != null && !algorithm.trim().isEmpty()) {
            // R03: algorithm 非法 → ALGORITHM_001
            throw new BizException(AlgorithmConstants.ALGORITHM_001, "不支持的哈希算法: " + algorithm);
        }
        if (algoEnum == null) {
            algoEnum = HashAlgorithmEnum.SHA_256;
        }

        HashResult result = new HashResult();
        result.setInput(input);
        result.setAlgorithm(algoEnum.name());

        try {
            String hashValue = computeHash(input, algoEnum);
            result.setHashValue(hashValue);
        } catch (NoSuchAlgorithmException e) {
            // 兜底：MessageDigest 异常时降级为 SHA-256 重试一次
            log.warn("哈希算法 {} 异常，降级为 SHA-256 重试", algoEnum.getDigestName());
            try {
                String fallbackHash = computeHash(input, HashAlgorithmEnum.SHA_256);
                result.setAlgorithm(HashAlgorithmEnum.SHA_256.name());
                result.setHashValue(fallbackHash);
            } catch (NoSuchAlgorithmException ex) {
                // 仍失败则返回空 hashValue + code=OK
                log.error("SHA-256 降级仍失败，返回空哈希值", ex);
                result.setHashValue("");
            }
        }

        return result;
    }

    @Override
    public BubbleSortResult bubbleSort(List<Integer> numbers, String order) {
        // R01: numbers null/空 → SORT_001
        if (numbers == null || numbers.isEmpty()) {
            throw new BizException(AlgorithmConstants.SORT_001, "排序数组为空");
        }

        // R02: order 空 → 默认 ASC
        // R03: order 非法 → 降级为 ASC
        SortOrderEnum orderEnum = SortOrderEnum.fromName(order);
        boolean desc = false;
        if (order != null && !order.trim().isEmpty() && orderEnum == null) {
            // 非法方向降级为 ASC，data.order 标注实际值
            log.warn("排序方向 {} 非法，降级为 ASC", order);
            orderEnum = SortOrderEnum.ASC;
        }
        if (orderEnum == null) {
            orderEnum = SortOrderEnum.ASC;
        }
        desc = (orderEnum == SortOrderEnum.DESC);

        BubbleSortResult result = new BubbleSortResult();
        result.setInput(new ArrayList<>(numbers));
        result.setOrder(orderEnum.name());

        // 兜底：数组过大(>1000) → 截取前 1000 元素
        boolean truncated = false;
        List<Integer> workNumbers = numbers;
        if (numbers.size() > AlgorithmConstants.MAX_SORT_ARRAY_SIZE) {
            log.warn("排序数组过大 {}，截取前 {} 元素", numbers.size(), AlgorithmConstants.MAX_SORT_ARRAY_SIZE);
            workNumbers = numbers.subList(0, AlgorithmConstants.MAX_SORT_ARRAY_SIZE);
            truncated = true;
        }

        // R04: 先 clone 再排序，不修改原始入参
        List<Integer> sortedList = new ArrayList<>(workNumbers);
        bubbleSortInternal(sortedList, desc);
        result.setSorted(sortedList);
        result.setTruncated(truncated);

        return result;
    }

    /**
     * 计算哈希值（十六进制）。
     *
     * @param input   输入字符串
     * @param algorithm 哈希算法
     * @return 十六进制哈希值
     * @throws NoSuchAlgorithmException 不支持的算法
     */
    private String computeHash(String input, HashAlgorithmEnum algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm.getDigestName());
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    /**
     * 字节数组转十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * 冒泡排序内部实现。
     *
     * @param arr  待排序列表（原地修改）
     * @param desc 是否降序
     */
    private void bubbleSortInternal(List<Integer> arr, boolean desc) {
        int n = arr.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                boolean needSwap;
                if (desc) {
                    needSwap = arr.get(j) < arr.get(j + 1);
                } else {
                    needSwap = arr.get(j) > arr.get(j + 1);
                }
                if (needSwap) {
                    Collections.swap(arr, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }
}
