package algoapi.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 冒泡排序服务
 * 算法逻辑复用 datastruct/src/排序/冒泡排序.java 的 sort(int[]) 实现
 * 不直接 import 中文包名，在此重写相同算法逻辑
 */
@Service
public class BubbleSortService {

    /**
     * 对数组进行冒泡排序（不改原数组，返回副本）
     */
    public int[] sort(int[] arr) {
        int[] a = Arrays.copyOf(arr, arr.length);
        int temp;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                if (a[j] > a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
        return a;
    }
}
