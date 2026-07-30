package com.demo.helloworld.enums;

import lombok.Getter;

/**
 * 健康状态枚举
 * 用于健康检查接口返回
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Getter
public enum HealthStatusEnum {

    UP("UP", "服务正常"),
    DOWN("DOWN", "服务异常");

    /** 状态码 */
    private final String code;

    /** 描述 */
    private final String desc;

    HealthStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
