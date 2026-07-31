package cn.wy.algodemo.controller;

import cn.wy.algodemo.model.BubbleSortRequest;
import cn.wy.algodemo.model.BubbleSortResponse;
import cn.wy.algodemo.model.ExportRequest;
import cn.wy.algodemo.model.HashRequest;
import cn.wy.algodemo.model.HashResponse;
import cn.wy.algodemo.service.BubbleSortService;
import cn.wy.algodemo.service.ExportService;
import cn.wy.algodemo.service.HashService;
import cn.wy.algodemo.service.HelloWorldService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 算法演示 REST 入口。
 *
 * <p>统一前缀 {@code /api}，提供四个接口：
 * <ul>
 *   <li>GET  /api/helloworld</li>
 *   <li>POST /api/hash</li>
 *   <li>POST /api/bubble-sort</li>
 *   <li>GET  /api/export?tab=all（内置示例，向后兼容）</li>
 *   <li>POST /api/export（接收前端实时结果，跨仓契约）</li>
 * </ul>
 * JSON 通信，CORS 由 {@link cn.wy.algodemo.config.CorsConfig} 全局放开，
 * 异常由 {@link GlobalExceptionHandler} 统一处理。</p>
 */
@RestController
@RequestMapping("/api")
public class AlgoController {

    private final HelloWorldService helloWorldService;
    private final HashService hashService;
    private final BubbleSortService bubbleSortService;
    private final ExportService exportService;

    /**
     * 显式构造器注入四个 Service（避免引入 Lombok 等额外依赖）。
     */
    public AlgoController(HelloWorldService helloWorldService,
                         HashService hashService,
                         BubbleSortService bubbleSortService,
                         ExportService exportService) {
        this.helloWorldService = helloWorldService;
        this.hashService = hashService;
        this.bubbleSortService = bubbleSortService;
        this.exportService = exportService;
    }

    /**
     * 接口 1：HelloWorld。
     *
     * @return {@code {"result":"Hello, World!","timestamp":"<ISO-8601 UTC>"}}
     */
    @GetMapping("/helloworld")
    public Map<String, Object> helloworld() {
        return helloWorldService.hello();
    }

    /**
     * 接口 2：哈希算法。
     *
     * @param request {@code {"input":"abc","algorithm":"SHA-256"}}
     * @return {@code {"input":"abc","algorithm":"SHA-256","hash":"...","length":64}}
     */
    @PostMapping("/hash")
    public HashResponse hash(@RequestBody HashRequest request) {
        return hashService.hash(request);
    }

    /**
     * 接口 3：冒泡排序。
     *
     * @param request {@code {"input":[5,3,8,1,9,2]}}
     * @return {@code {"input":[...],"sorted":[...],"steps":[...],"swapCount":8}}
     */
    @PostMapping("/bubble-sort")
    public BubbleSortResponse bubbleSort(@RequestBody BubbleSortRequest request) {
        return bubbleSortService.sort(request);
    }

    /**
     * 接口 4：导出（GET，向后兼容）。
     *
     * <p>以纯文本附件形式下载，文件名 {@code algo-export-<yyyyMMddHHmmss>.txt}（UTC）。
     * 使用内置固定示例数据生成内容。</p>
     *
     * @param tab       helloworld | hash | bubble-sort | all（默认 all）
     * @param response  HTTP 响应，用于设置头与输出流
     */
    @GetMapping("/export")
    public void export(@RequestParam(value = "tab", defaultValue = "all") String tab,
                       HttpServletResponse response) throws IOException {
        String content = exportService.buildContent(tab);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + exportService.generateFileName() + "\"");
        response.setContentLength(bytes.length);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }

    /**
     * 接口 4：导出（POST，跨仓契约）。
     *
     * <p>接收前端各 Tab 已执行的实际结果（{@link ExportRequest}），由后端格式化为纯文本附件下载。
     * 请求体中缺省的模块字段由后端用内置示例兜底。文件名 {@code algo-export-<yyyyMMddHHmmss>.txt}（UTC）。</p>
     *
     * @param request  导出请求体（tab / hello / hash / bubble 均可选）
     * @param response HTTP 响应，用于设置头与输出流
     */
    @PostMapping("/export")
    public void exportPost(@RequestBody ExportRequest request, HttpServletResponse response) throws IOException {
        String content = exportService.buildContent(request);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + exportService.generateFileName() + "\"");
        response.setContentLength(bytes.length);
        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }
}
