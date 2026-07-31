package cn.wy.algodemo.service;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.ExportRequest;
import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 导出服务。
 *
 * <p>按 tab 生成纯文本导出内容，含 HelloWorld / Hash / BubbleSort 三段，
 * 以 {@code ===== XXX =====} 分隔；并生成 {@code algo-export-<yyyyMMddHHmmss>.txt} 文件名。</p>
 *
 * <p>支持两种入口：
 * <ul>
 *   <li>{@link #buildContent(String)}：GET 导出，使用内置固定示例（向后兼容）；</li>
 *   <li>{@link #buildContent(ExportRequest)}：POST 导出，优先采用前端实时结果，
 *       缺省字段用内置示例兜底。</li>
 * </ul></p>
 */
@Service
public class ExportService {

    /** 导出文件名时间戳格式（UTC）。 */
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 导出三段标题。 */
    private static final String HEADER_HELLO = "===== HelloWorld =====";
    private static final String HEADER_HASH = "===== Hash =====";
    private static final String HEADER_BUBBLE = "===== BubbleSort =====";

    /** 导出固定示例：哈希输入与算法。 */
    private static final String DEMO_HASH_INPUT = "abc";
    private static final String DEMO_HASH_ALGORITHM = "SHA-256";

    /** 导出固定示例：冒泡排序输入数组。 */
    private static final List<Integer> DEMO_BUBBLE_INPUT = Arrays.asList(5, 3, 8, 1, 9, 2);

    private final HelloWorldService helloWorldService;
    private final HashService hashService;
    private final BubbleSortService bubbleSortService;

    /**
     * 显式构造器注入三个算法服务。
     */
    public ExportService(HelloWorldService helloWorldService,
                         HashService hashService,
                         BubbleSortService bubbleSortService) {
        this.helloWorldService = helloWorldService;
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
    }

    /**
     * 生成导出文件名（UTC 时区）。
     *
     * @return 形如 {@code algo-export-20260731120000.txt}
     */
    public String generateFileName() {
        return "algo-export-" + ZonedDateTime.now(ZoneOffset.UTC).format(FILE_TS) + ".txt";
    }

    /**
     * 按 tab 生成纯文本导出内容（GET 入口，使用内置固定示例）。
     *
     * @param tab helloworld | hash | bubble-sort | all（为空或未知值按 all 处理）
     * @return 纯文本内容
     */
    public String buildContent(String tab) {
        String normalized = normalizeTab(tab);
        StringBuilder sb = new StringBuilder();
        if ("all".equals(normalized) || "helloworld".equals(normalized)) {
            Map<String, Object> hw = helloWorldService.hello();
            appendHello(sb, String.valueOf(hw.get("result")), String.valueOf(hw.get("timestamp")));
        }
        if ("all".equals(normalized) || "hash".equals(normalized)) {
            HashResponse hr = hashDemo();
            appendSeparatorIfNeeded(sb);
            appendHash(sb, hr.getInput(), hr.getAlgorithm(), hr.getHash(), hr.getLength());
        }
        if ("all".equals(normalized) || "bubble-sort".equals(normalized)) {
            BubbleSortResponse bs = bubbleDemo();
            appendSeparatorIfNeeded(sb);
            appendBubble(sb, bs.getInput(), bs.getSorted(), bs.getSwapCount(), bs.getSteps());
        }
        return sb.toString();
    }

    /**
     * 按导出请求生成纯文本内容（POST 入口，优先采用前端实时结果，缺省字段用示例兜底）。
     *
     * @param req 导出请求体，各模块字段可选
     * @return 纯文本内容
     */
    public String buildContent(ExportRequest req) {
        String normalized = normalizeTab(req == null ? null : req.getTab());
        StringBuilder sb = new StringBuilder();
        if ("all".equals(normalized) || "helloworld".equals(normalized)) {
            ExportRequest.HelloResult hello = req == null ? null : req.getHello();
            String result;
            String timestamp;
            if (hello != null) {
                result = String.valueOf(hello.getResult());
                timestamp = String.valueOf(hello.getTimestamp());
            } else {
                Map<String, Object> hw = helloWorldService.hello();
                result = String.valueOf(hw.get("result"));
                timestamp = String.valueOf(hw.get("timestamp"));
            }
            appendHello(sb, result, timestamp);
        }
        if ("all".equals(normalized) || "hash".equals(normalized)) {
            HashResponse hr = (req != null && req.getHash() != null) ? req.getHash() : hashDemo();
            appendSeparatorIfNeeded(sb);
            appendHash(sb, hr.getInput(), hr.getAlgorithm(), hr.getHash(), hr.getLength());
        }
        if ("all".equals(normalized) || "bubble-sort".equals(normalized)) {
            BubbleSortResponse bs = (req != null && req.getBubble() != null) ? req.getBubble() : bubbleDemo();
            appendSeparatorIfNeeded(sb);
            appendBubble(sb, bs.getInput(), bs.getSorted(), bs.getSwapCount(), bs.getSteps());
        }
        return sb.toString();
    }

    /**
     * 归一化 tab：null/空/未知值统一为 all。
     */
    private String normalizeTab(String tab) {
        if (tab == null || tab.isEmpty()) {
            return "all";
        }
        if ("helloworld".equals(tab) || "hash".equals(tab)
                || "bubble-sort".equals(tab) || "all".equals(tab)) {
            return tab;
        }
        return "all";
    }

    private HashResponse hashDemo() {
        HashRequest req = new HashRequest();
        req.setInput(DEMO_HASH_INPUT);
        req.setAlgorithm(DEMO_HASH_ALGORITHM);
        return hashService.hash(req);
    }

    private BubbleSortResponse bubbleDemo() {
        BubbleSortRequest req = new BubbleSortRequest();
        req.setInput(new ArrayList<>(DEMO_BUBBLE_INPUT));
        return bubbleSortService.sort(req);
    }

    private void appendSeparatorIfNeeded(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
    }

    private void appendHello(StringBuilder sb, String result, String timestamp) {
        sb.append(HEADER_HELLO).append("\n");
        sb.append("result: ").append(result).append("\n");
        sb.append("timestamp: ").append(timestamp).append("\n");
    }

    private void appendHash(StringBuilder sb, String input, String algorithm, String hash, int length) {
        sb.append(HEADER_HASH).append("\n");
        sb.append("input: ").append(input).append("\n");
        sb.append("algorithm: ").append(algorithm).append("\n");
        sb.append("hash: ").append(hash).append("\n");
        sb.append("length: ").append(length).append("\n");
    }

    private void appendBubble(StringBuilder sb, List<Integer> input, List<Integer> sorted,
                              int swapCount, List<BubbleSortResponse.Step> steps) {
        sb.append(HEADER_BUBBLE).append("\n");
        sb.append("input: ").append(input).append("\n");
        sb.append("sorted: ").append(sorted).append("\n");
        sb.append("swapCount: ").append(swapCount).append("\n");
        sb.append("steps:\n");
        for (BubbleSortResponse.Step step : steps) {
            sb.append("  round ").append(step.getRound())
                    .append(": swaps=").append(step.getSwaps())
                    .append(", array=").append(step.getArray()).append("\n");
        }
    }
}
