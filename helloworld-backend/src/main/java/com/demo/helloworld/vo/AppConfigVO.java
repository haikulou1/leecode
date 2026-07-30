package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用配置返回 VO
 * 对应接口：W06 GET /api/config
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应用名称 */
    private String appName;

    /** 主题配色 */
    private String theme;

    /** 默认语言 */
    private String language;
}
