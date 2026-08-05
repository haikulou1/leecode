package cn.wy.helloworld.controller;

import cn.wy.helloworld.common.Result;
import cn.wy.helloworld.util.HashUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口控制器：helloworld 与 哈希算法。
 *
 * @author dtcoder
 */
@RestController
@RequestMapping("/api")
public class HelloWorldController {

    /**
     * 接口一：helloworld。
     *
     * <p>GET /api/helloworld —— 最简健康检查/演示端点。</p>
     *
     * @return 统一响应体，data 为 "Hello, World!"
     */
    @GetMapping("/helloworld")
    public Result<String> helloWorld() {
        return Result.success("Hello, World!");
    }

    /**
     * 接口二：哈希算法。
     *
     * <p>GET /api/hash —— 对给定明文计算哈希（小写十六进制）。</p>
     *
     * @param input 必填，待计算哈希的明文
     * @param algo 可选，算法名，取值 MD5 / SHA-1 / SHA-256 / SHA-512，默认 SHA-256
     * @return 统一响应体，data 包含 algorithm/input/hex 三个字段；
     *         入参非法时返回 code=400 的失败响应
     */
    @GetMapping("/hash")
    public Result<Map<String, String>> hash(
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "algo", required = false) String algo) {

        // 参数校验：input 必填
        if (input == null || input.isEmpty()) {
            return Result.fail(Result.CODE_BAD_REQUEST, "input is required");
        }

        // 参数校验：algo 默认 SHA-256，非法时给出明确错误
        String algorithm = (algo == null || algo.isEmpty()) ? HashUtil.DEFAULT_ALGORITHM : algo;
        if (!HashUtil.isSupported(algorithm)) {
            return Result.fail(Result.CODE_BAD_REQUEST, "unsupported algorithm: " + algorithm);
        }

        String hex = HashUtil.hash(input, algorithm);

        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("algorithm", algorithm.toUpperCase());
        data.put("input", input);
        data.put("hex", hex);

        return Result.success(data);
    }
}
