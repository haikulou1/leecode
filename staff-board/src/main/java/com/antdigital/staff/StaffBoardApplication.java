package com.antdigital.staff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 人员看板应用入口。
 */
@EnableAsync
@SpringBootApplication
public class StaffBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaffBoardApplication.class, args);
    }
}