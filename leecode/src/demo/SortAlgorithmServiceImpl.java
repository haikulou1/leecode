package demo;

import java.util.Arrays;

/**
 * 排序算法接口实现：快速排序（升序）
 */
public class SortAlgorithmServiceImpl implements SortAlgorithmService {

    @Override
    public int[] sort(int[] arr) {
        if (arr == null) {
            return new int[0];
        }
        int[] copy = Arrays.copyOf(arr, arr.length);
        quickSort(copy, 0, copy.length - 1);
        return copy;
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivot = partition(arr, low, high);
        quickSort(arr, low, pivot - 1);
        quickSort(arr, pivot + 1, high);
    }

    private int partition(int[] arr, int low, int high) {
        int key = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= key) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
