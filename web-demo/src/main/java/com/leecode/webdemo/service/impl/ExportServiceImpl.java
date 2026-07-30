package com.leecode.webdemo.service.impl;

import com.leecode.webdemo.common.AlgorithmConstants;
import com.leecode.webdemo.common.BizException;
import com.leecode.webdemo.dto.BubbleSortResult;
import com.leecode.webdemo.dto.ExportRequest;
import com.leecode.webdemo.dto.HashResult;
import com.leecode.webdemo.enums.ExportFormatEnum;
import com.leecode.webdemo.enums.ExportTypeEnum;
import com.leecode.webdemo.service.AlgorithmService;
import com.leecode.webdemo.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 导出服务实现，复用算法服务，格式化为 CSV/JSON 文件流，含兜底降级。
 *
 * @author DTCoder
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private final AlgorithmService algorithmService;

    public ExportServiceImpl(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @Override
    public byte[] export(ExportRequest request) {
        // R01: type 非法 → EXPORT_001
        ExportTypeEnum typeEnum = ExportTypeEnum.fromName(request.getType());
        if (typeEnum == null) {
            throw new BizException(AlgorithmConstants.EXPORT_001, "不支持的导出类型: " + request.getType());
        }

        // R02: format 非法 → 降级为 CSV
        ExportFormatEnum formatEnum = ExportFormatEnum.fromName(request.getFormat());
        if (formatEnum == null && request.getFormat() != null && !request.getFormat().trim().isEmpty()) {
            log.warn("导出格式 {} 非法，降级为 CSV", request.getFormat());
            formatEnum = ExportFormatEnum.CSV;
        }
        if (formatEnum == null) {
            formatEnum = ExportFormatEnum.CSV;
        }

        try {
            // R05: 复用算法服务
            String content;
            switch (typeEnum) {
                case HELLO_WORLD:
                    content = formatHelloWorld(formatEnum);
                    break;
                case HASH:
                    content = formatHash(request, formatEnum);
                    break;
                case BUBBLE_SORT:
                    content = formatBubbleSort(request, formatEnum);
                    break;
                default:
                    content = "";
            }
            return content.getBytes(StandardCharsets.UTF_8);
        } catch (BizException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // 兜底：算法服务异常 → 返回仅含表头的空 CSV 或空 JSON
            log.error("导出算法服务异常，触发兜底导出: ", e);
            return fallbackContent(formatEnum).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public String getFileExtension(ExportRequest request) {
        ExportFormatEnum formatEnum = ExportFormatEnum.fromName(request.getFormat());
        if (formatEnum == null) {
            formatEnum = ExportFormatEnum.CSV;
        }
        return formatEnum.name().toLowerCase();
    }

    /**
     * 格式化 HelloWorld 导出内容。
     */
    private String formatHelloWorld(ExportFormatEnum formatEnum) {
        String message = algorithmService.helloWorld();
        if (formatEnum == ExportFormatEnum.JSON) {
            return "{\"message\":\"" + escapeJson(message) + "\"}";
        }
        return "field,value\n" + "message," + escapeCsv(message) + "\n";
    }

    /**
     * 格式化哈希导出内容。
     */
    private String formatHash(ExportRequest request, ExportFormatEnum formatEnum) {
        // R03: type=HASH 缺 hashInput → EXPORT_003
        if (request.getHashInput() == null || request.getHashInput().trim().isEmpty()) {
            throw new BizException(AlgorithmConstants.EXPORT_003, "哈希导出缺少输入");
        }
        HashResult result = algorithmService.hash(request.getHashInput(), request.getHashAlgorithm());
        if (formatEnum == ExportFormatEnum.JSON) {
            return "{\"input\":\"" + escapeJson(result.getInput()) + "\","
                    + "\"algorithm\":\"" + escapeJson(result.getAlgorithm()) + "\","
                    + "\"hashValue\":\"" + escapeJson(result.getHashValue()) + "\"}";
        }
        return "field,value\n"
                + "input," + escapeCsv(result.getInput()) + "\n"
                + "algorithm," + escapeCsv(result.getAlgorithm()) + "\n"
                + "hashValue," + escapeCsv(result.getHashValue()) + "\n";
    }

    /**
     * 格式化冒泡排序导出内容。
     */
    private String formatBubbleSort(ExportRequest request, ExportFormatEnum formatEnum) {
        // R04: type=BUBBLE_SORT 缺 sortNumbers → EXPORT_004
        if (request.getSortNumbers() == null || request.getSortNumbers().isEmpty()) {
            throw new BizException(AlgorithmConstants.EXPORT_004, "排序导出缺少数组");
        }
        BubbleSortResult result = algorithmService.bubbleSort(request.getSortNumbers(), request.getSortOrder());
        if (formatEnum == ExportFormatEnum.JSON) {
            return buildBubbleSortJson(result);
        }
        return "field,value\n"
                + "input," + escapeCsv(joinList(result.getInput())) + "\n"
                + "sorted," + escapeCsv(joinList(result.getSorted())) + "\n"
                + "order," + escapeCsv(result.getOrder()) + "\n"
                + (result.getTruncated() != null && result.getTruncated() ? "truncated,true\n" : "");
    }

    /**
     * 构建冒泡排序 JSON。
     */
    private String buildBubbleSortJson(BubbleSortResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"input\":").append(toJsonArray(result.getInput()));
        sb.append(",\"sorted\":").append(toJsonArray(result.getSorted()));
        sb.append(",\"order\":\"").append(escapeJson(result.getOrder())).append("\"");
        if (result.getTruncated() != null && result.getTruncated()) {
            sb.append(",\"truncated\":true");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 兜底内容：仅含表头的空 CSV 或空 JSON。
     */
    private String fallbackContent(ExportFormatEnum formatEnum) {
        if (formatEnum == ExportFormatEnum.JSON) {
            return "{}";
        }
        return "field,value\n";
    }

    /**
     * 列表转逗号分隔字符串。
     */
    private String joinList(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /**
     * 整型列表转 JSON 数组字符串。
     */
    private String toJsonArray(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * CSV 值转义：含逗号或引号时用双引号包裹。
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * JSON 字符串转义。
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
