package com.demo.helloworld.service;

import com.demo.helloworld.vo.AppConfigVO;

/**
 * 应用配置 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface ConfigService {

    /**
     * 获取应用配置
     *
     * @return 应用配置 VO
     */
    AppConfigVO getConfig();
}
