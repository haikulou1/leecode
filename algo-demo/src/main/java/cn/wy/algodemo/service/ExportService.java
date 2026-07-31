package cn.wy.algodemo.service;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

/**
 * 导出服务。
 *
 * <p>按 tab 生成纯文本导出内容，含 HelloWorld / Hash / BubbleSort 三段，
 * 以 {@code ===== XXX =====} 分隔；并生成 {@code algo-export-<yyyyMMddHHmmss>.txt} 文件名。</p>
 */
@Service
public class ExportService {

    /** 导出文件名时间戳格式。 */
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 导出三段标题。 */
    private static final String HEADER_HELLO = "===== HelloWorld =====";
    private static final String HEADER_HASH = "===== Hash =====";
    private static final String HEADER_BUBBLE = "===== BubbleSort =====";

    /** 导出固定示例：哈希输入与算法。 */
    private static final String DEMO_HASH_INPUT = "abc";
    private static final String DEMO_HASH_ALGORITHM = "SHA-256";

    /** 导出固定示例：冒泡排序输入数组。 */
    private static final String DEMO_BUBBLE_INPUT = "[5, 3, 8, 1, 9, 2]";

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
     * 生成导出文件名。
     *
     * @return 形如 {@code algo-export-20260731120000.txt}
     */
    public String generateFileName() {
        return "algo-export-" + LocalDateTime.now().format(FILE_TS) + ".txt";
    }

    /**
     * 按 tab 生成纯文本导出内容。
     *
     * @param tab helloworld | hash | bubble-sort | all（为空或未知值按 all 处理）
     * @return 纯文本内容
     */
    public String buildContent(String tab) {
        if (tab == null || tab.isEmpty()) {
            tab = "all";
        }
        if (!"helloworld".equals(tab) && !"hash".equals(tab)
                && !"bubble-sort".equals(tab) && !"all".equals(tab)) {
            tab = "all";
        }

        StringBuilder sb = new StringBuilder();
        if ("all".equals(tab) || "helloworld".equals(tab)) {
            appendHello(sb);
        }
        if ("all".equals(tab) || "hash".equals(tab)) {
            appendSeparatorIfNeeded(sb);
            appendHash(sb);
        }
        if ("all".equals(tab) || "bubble-sort".equals(tab)) {
            appendSeparatorIfNeeded(sb);
            appendBubble(sb);
        }
        return sb.toString();
    }

    private void appendSeparatorIfNeeded(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
    }

    private void appendHello(StringBuilder sb) {
        Map<String, Object> hw = helloWorldService.hello();
        sb.append(HEADER_HELLO).append("\n");
        sb.append("result: ").append(hw.get("result")).append("\n");
        sb.append("timestamp: ").append(hw.get("timestamp")).append("\n");
    }

    private void appendHash(StringBuilder sb) {
        HashRequest req = new HashRequest();
        req.setInput(DEMO_HASH_INPUT);
        req.setAlgorithm(DEMO_HASH_ALGORITHM);
        HashResponse hr = hashService.hash(req);
        sb.append(HEADER_HASH).append("\n");
        sb.append("input: ").append(hr.getInput()).append("\n");
        sb.append("algorithm: ").append(hr.getAlgorithm()).append("\n");
        sb.append("hash: ").append(hr.getHash()).append("\n");
        sb.append("length: ").append(hr.getLength()).append("\n");
    }

    private void appendBubble(StringBuilder sb) {
        BubbleSortRequest req = new BubbleSortRequest();
        req.setInput(Arrays.asList(5, 3, 8, 1, 9, 2));
        BubbleSortResponse bs = bubbleSortService.sort(req);
        sb.append(HEADER_BUBBLE).append("\n");
        sb.append("input: ").append(DEMO_BUBBLE_INPUT).append("\n");
        sb.append("sorted: ").append(bs.getSorted()).append("\n");
        sb.append("swapCount: ").append(bs.getSwapCount()).append("\n");
        sb.append("steps:\n");
        for (BubbleSortResponse.Step step : bs.getSteps()) {
            sb.append("  round ").append(step.getRound())
                    .append(": swaps=").append(step.getSwaps())
                    .append(", array=").append(step.getArray()).append("\n");
        }
    }
}
