import java.util.Arrays;

/**
 * 排序算法工具类（演示冒泡、快速、选择三种常用算法，升序）。
 */
public class SortUtil {

    /**
     * 对整数数组执行指定算法排序（升序）。
     *
     * @param algorithm 算法名：bubble / quicksort / selection
     * @param arr       待排序数组
     * @return 排序后的新数组（不修改原数组）
     * @throws IllegalArgumentException 算法不支持或参数缺失
     */
    public static int[] sort(String algorithm, int[] arr) {
        int[] result = arr == null ? new int[0] : Arrays.copyOf(arr, arr.length);
        String alg = normalize(algorithm);
        switch (alg) {
            case "bubble":
                bubbleSort(result);
                break;
            case "quicksort":
                quickSort(result, 0, result.length - 1);
                break;
            case "selection":
                selectionSort(result);
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        return result;
    }

    /**
     * 校验算法是否被支持。
     */
    public static boolean isSupported(String algorithm) {
        if (algorithm == null || algorithm.isEmpty()) {
            return false;
        }
        switch (algorithm.trim().toLowerCase()) {
            case "bubble":
            case "quicksort":
            case "selection":
                return true;
            default:
                return false;
        }
    }

    private static String normalize(String algorithm) {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: algorithm");
        }
        return algorithm.trim().toLowerCase();
    }

    /** 冒泡排序。 */
    private static void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /** 快速排序（Hoare 分区）。 */
    private static void quickSort(int[] a, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivot = a[low + (high - low) / 2];
        int i = low, j = high;
        while (i <= j) {
            while (a[i] < pivot) {
                i++;
            }
            while (a[j] > pivot) {
                j--;
            }
            if (i <= j) {
                swap(a, i, j);
                i++;
                j--;
            }
        }
        if (low < j) {
            quickSort(a, low, j);
        }
        if (i < high) {
            quickSort(a, i, high);
        }
    }

    /** 选择排序。 */
    private static void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            if (min != i) {
                swap(a, i, min);
            }
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
