package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 健康检查返回 VO
 * 对应接口：W05 GET /api/health
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 健康状态：UP/DOWN */
    private String status;

    /** 检查时间 */
    private String timestamp;
}
