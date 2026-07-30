package com.leecode.webdemo.controller;

import com.leecode.webdemo.common.ApiResponse;
import com.leecode.webdemo.dto.BubbleSortRequest;
import com.leecode.webdemo.dto.BubbleSortResult;
import com.leecode.webdemo.dto.ExportRequest;
import com.leecode.webdemo.dto.HashRequest;
import com.leecode.webdemo.dto.HashResult;
import com.leecode.webdemo.service.AlgorithmService;
import com.leecode.webdemo.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 算法演示 REST 接口控制器。
 *
 * <p>提供 HelloWorld、哈希计算、冒泡排序、结果导出四个端点。</p>
 *
 * @author DTCoder
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AlgorithmController {

    private final AlgorithmService algorithmService;
    private final ExportService exportService;

    public AlgorithmController(AlgorithmService algorithmService, ExportService exportService) {
        this.algorithmService = algorithmService;
        this.exportService = exportService;
    }

    /**
     * W01 HelloWorld 接口。
     *
     * @return 统一响应，data.message = "Hello World"
     */
    @GetMapping("/helloworld")
    public ApiResponse<Map<String, String>> helloWorld() {
        log.info("调用 HelloWorld 接口");
        Map<String, String> data = new HashMap<>(1);
        try {
            data.put("message", algorithmService.helloWorld());
        } catch (Exception e) {
            // 兜底：Service 异常返回固定值
            log.error("HelloWorld 接口异常，触发兜底: ", e);
            data.put("message", "Hello World");
        }
        return ApiResponse.success(data);
    }

    /**
     * W02 哈希计算接口。
     *
     * @param request 哈希请求
     * @return 统一响应，data.{input, algorithm, hashValue}
     */
    @PostMapping("/hash")
    public ApiResponse<HashResult> hash(@Valid @RequestBody HashRequest request) {
        log.info("调用哈希接口, input length={}, algorithm={}",
                request.getInput() != null ? request.getInput().length() : 0, request.getAlgorithm());
        HashResult result = algorithmService.hash(request.getInput(), request.getAlgorithm());
        return ApiResponse.success(result);
    }

    /**
     * W03 冒泡排序接口。
     *
     * @param request 排序请求
     * @return 统一响应，data.{input, sorted, order, truncated}
     */
    @PostMapping("/bubbleSort")
    public ApiResponse<BubbleSortResult> bubbleSort(@Valid @RequestBody BubbleSortRequest request) {
        log.info("调用冒泡排序接口, numbers size={}, order={}",
                request.getNumbers() != null ? request.getNumbers().size() : 0, request.getOrder());
        BubbleSortResult result = algorithmService.bubbleSort(request.getNumbers(), request.getOrder());
        return ApiResponse.success(result);
    }

    /**
     * W04 结果导出接口。
     *
     * @param request 导出请求
     * @return 文件流（CSV / JSON）
     */
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExportRequest request) {
        log.info("调用导出接口, type={}, format={}", request.getType(), request.getFormat());
        byte[] content = exportService.export(request);
        String extension = exportService.getFileExtension(request);
        String filename = request.getType().toLowerCase() + "_result." + extension;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
