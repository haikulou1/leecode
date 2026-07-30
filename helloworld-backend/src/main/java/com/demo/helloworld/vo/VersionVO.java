package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 版本信息返回 VO
 * 对应接口：W07 GET /api/version
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 版本号 */
    private String version;

    /** 构建时间 */
    private String buildTime;
}
