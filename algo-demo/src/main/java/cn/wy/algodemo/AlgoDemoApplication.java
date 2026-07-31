package cn.wy.algodemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类。
 *
 * <p>承载四个 REST 接口：helloworld / 哈希算法 / 冒泡排序 / 导出，
 * 所有接口前缀 {@code /api}，监听端口 8080（见 application.yml）。</p>
 */
@SpringBootApplication
public class AlgoDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgoDemoApplication.class, args);
    }
}
