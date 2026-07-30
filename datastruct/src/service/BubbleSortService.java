package service;

import util.JsonUtil;

/**
 * 冒泡排序服务：F1.3 接口。
 * 经典冒泡排序（升序），纯函数：复制入参数组，不修改原数组，统计交换次数。
 */
public class BubbleSortService {

    /**
     * 对 data（逗号分隔整数）执行升序冒泡排序。
     * 返回 {"input":[...],"sorted":[...],"steps":N,"asc":true}
     */
    public String sort(String data) {
        int[] values = parse(data);
        int[] sorted = values.clone();
        int steps = bubbleSortAsc(sorted);
        StringBuilder sb = new StringBuilder(128);
        sb.append('{');
        sb.append(JsonUtil.rawField("input", toJsonArray(values)));
        sb.append(',');
        sb.append(JsonUtil.rawField("sorted", toJsonArray(sorted)));
        sb.append(',');
        sb.append(JsonUtil.rawField("steps", String.valueOf(steps)));
        sb.append(',');
        sb.append(JsonUtil.rawField("asc", "true"));
        sb.append('}');
        return sb.toString();
    }

    /**
     * 解析逗号分隔整数字符串为 int[]。
     * 校验：1~50 个整数，每个 [-1000,1000]。
     * 非法时抛 IllegalArgumentException，由 RouterHandler 转为 400。
     */
    private int[] parse(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data format invalid");
        }
        String[] parts = data.split(",", -1);
        if (parts.length < 1 || parts.length > 50) {
            throw new IllegalArgumentException("data format invalid");
        }
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.isEmpty()) {
                throw new IllegalArgumentException("data format invalid");
            }
            int v;
            try {
                v = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("data format invalid");
            }
            if (v < -1000 || v > 1000) {
                throw new IllegalArgumentException("data format invalid");
            }
            result[i] = v;
        }
        return result;
    }

    /**
     * 经典冒泡排序（升序），返回交换次数。
     * 双层 for 循环：if a[j] > a[j+1] 交换。
     */
    private int bubbleSortAsc(int[] a) {
        int n = a.length;
        int steps = 0;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    steps++;
                }
            }
        }
        return steps;
    }

    /**
     * 将 int[] 序列化为 JSON 数组字符串，如 [1,3,5,8]。
     */
    private String toJsonArray(int[] a) {
        StringBuilder sb = new StringBuilder(a.length * 4 + 2);
        sb.append('[');
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(a[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
