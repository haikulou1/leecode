package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 欢迎语返回 VO
 * 对应接口：W01 GET /api/hello
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 欢迎语文案 */
    private String message;

    /** 语言标识 */
    private String language;
}
