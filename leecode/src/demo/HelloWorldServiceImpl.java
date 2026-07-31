package demo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * HelloWorld 接口实现
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String hello() {
        return "Hello, World! 当前时间: "
                + DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now());
    }
}
