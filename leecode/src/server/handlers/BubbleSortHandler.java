package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ApiServer;
import server.model.ApiResponse;

import java.util.Arrays;
import java.util.Random;

/**
 * GET /api/bubble-sort
 * 入参：无（后端生成 8 个 [0,100] 随机整数）
 * 出参：{"code":200,"msg":"success","data":{"original":[...],"sorted":[...],"count":8}}
 * 算法：标准冒泡排序（升序），返回排序前后对比
 */
public class BubbleSortHandler implements HttpHandler {

    private static final int SIZE = 8;
    private static final int BOUND = 101; // nextInt(101) -> [0,100]

    @Override
    public void handle(HttpExchange exchange) {
        if (ApiServer.handleOptions(exchange)) {
            return;
        }
        try {
            int[] original = generateRandom();
            int[] sorted = bubbleSort(Arrays.copyOf(original, original.length));
            String dataJson = "{\"original\":" + toArrayJson(original)
                    + ",\"sorted\":" + toArrayJson(sorted)
                    + ",\"count\":" + original.length + "}";
            ApiServer.writeJson(exchange, ApiResponse.ok(dataJson), 200);
        } catch (Exception e) {
            ApiServer.writeError(exchange, 500, "internal error: " + e.getMessage());
        }
    }

    /**
     * 业务逻辑：标准冒泡排序（升序），返回排序后数组，供 ExportHandler 复用。
     * 直接修改传入数组并返回（调用方应传副本）。
     */
    public static int[] bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
        return arr;
    }

    /** 生成 8 个 [0,100] 随机整数 */
    public static int[] generateRandom() {
        Random random = new Random();
        int[] arr = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            arr[i] = random.nextInt(BOUND);
        }
        return arr;
    }

    private static String toArrayJson(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
