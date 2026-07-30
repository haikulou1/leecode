package algoapi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BubbleSortService 单元测试
 * 验证算法正确性 + 边界：单元素/空数组/不改原数组
 * I2: 满足 clarify.md §4.6 测试要求
 */
class BubbleSortServiceTest {

    private final BubbleSortService bubbleSortService = new BubbleSortService();

    @Test
    void sort_normalArray_returnsSortedCopy() {
        int[] input = {5, 3, 8, 1, 9, 2};
        int[] sorted = bubbleSortService.sort(input);
        assertArrayEquals(new int[]{1, 2, 3, 5, 8, 9}, sorted);
    }

    @Test
    void sort_singleElement_returnsSame() {
        int[] input = {42};
        int[] sorted = bubbleSortService.sort(input);
        assertArrayEquals(new int[]{42}, sorted);
    }

    @Test
    void sort_emptyArray_returnsEmpty() {
        int[] input = {};
        int[] sorted = bubbleSortService.sort(input);
        assertArrayEquals(new int[]{}, sorted);
    }

    @Test
    void sort_doesNotModifyOriginalArray() {
        int[] input = {3, 1, 2};
        int[] original = input.clone();
        bubbleSortService.sort(input);
        assertArrayEquals(original, input);
    }

    @Test
    void sort_alreadySorted_remainsSorted() {
        int[] input = {1, 2, 3, 4, 5};
        int[] sorted = bubbleSortService.sort(input);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, sorted);
    }
}
