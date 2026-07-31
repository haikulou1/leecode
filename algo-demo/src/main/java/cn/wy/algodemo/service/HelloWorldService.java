package cn.wy.algodemo.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HelloWorld 演示服务。
 *
 * <p>返回固定文本 {@code Hello, World!} 与 ISO-8601 UTC 时间戳。</p>
 */
@Service
public class HelloWorldService {

    /**
     * 产生 HelloWorld 结果。
     *
     * @return 包含 {@code result} 与 {@code timestamp} 的有序 map
     */
    public Map<String, Object> hello() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result", "Hello, World!");
        result.put("timestamp", Instant.now().toString());
        return result;
    }
}
