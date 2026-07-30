package com.demo.helloworld;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HelloWorld 后端应用主启动类
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@SpringBootApplication
@MapperScan("com.demo.helloworld.mapper")
public class HelloWorldApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }
}
