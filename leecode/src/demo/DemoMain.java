package demo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 演示入口：执行三个接口（HelloWorld / 哈希 / 排序）并导出各页面结果
 *
 * 运行：在 leecode/src 目录执行
 *   javac demo/*.java
 *   java demo.DemoMain
 */
public class DemoMain {

    public static void main(String[] args) {
        HelloWorldService helloService = new HelloWorldServiceImpl();
        HashAlgorithmService hashService = new HashAlgorithmServiceImpl();
        SortAlgorithmService sortService = new SortAlgorithmServiceImpl();
        ExportService exportService = new ExportServiceImpl();

        // 1. HelloWorld 接口
        String hello = helloService.hello();
        System.out.println("[HelloWorld] result = " + hello);

        // 2. 哈希算法接口
        String md5 = hashService.hash("DTCoder", "MD5");
        String sha256 = hashService.hash("DTCoder", "SHA-256");
        String hashResult = "{\"input\":\"DTCoder\",\"MD5\":\"" + md5 + "\",\"SHA-256\":\"" + sha256 + "\"}";
        System.out.println("[Hash] result = " + hashResult);

        // 3. 排序算法接口
        int[] origin = {5, 3, 8, 1, 9, 2, 7, 4, 6, 0};
        int[] sorted = sortService.sort(origin);
        String sortResult = "{\"origin\":" + Arrays.toString(origin) + ",\"sorted\":" + Arrays.toString(sorted) + "}";
        System.out.println("[Sort] result = " + sortResult);

        // 4. 导出各页面结果（后台提供导出接口）
        String ts = now();
        String f1 = exportService.export("HelloWorld", hello, ts);
        String f2 = exportService.export("HashAlgorithm", hashResult, ts);
        String f3 = exportService.export("SortAlgorithm", sortResult, ts);

        System.out.println("导出完成:");
        System.out.println("  - " + f1);
        System.out.println("  - " + f2);
        System.out.println("  - " + f3);
    }

    private static String now() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }
}
