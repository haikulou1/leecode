package demo;

/**
 * 排序算法接口
 * 对应前端第三个 tab：展示排序算法执行结果
 */
public interface SortAlgorithmService {

    /**
     * 对整数数组进行升序排序
     *
     * @param arr 原始数组
     * @return 排序后的新数组
     */
    int[] sort(int[] arr);
}
