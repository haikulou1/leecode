package service;

import util.JsonUtil;

/**
 * HelloWorld 服务：F1.1 接口。
 * 返回 {"result":"Hello World","timestamp":<ts>}
 */
public class HelloWorldService {

    /**
     * 返回固定字符串 "Hello World" 及当前毫秒时间戳的 JSON。
     */
    public String hello() {
        long ts = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder(64);
        sb.append('{');
        sb.append(JsonUtil.quoteField("result", "Hello World"));
        sb.append(',');
        sb.append(JsonUtil.rawField("timestamp", String.valueOf(ts)));
        sb.append('}');
        return sb.toString();
    }
}
