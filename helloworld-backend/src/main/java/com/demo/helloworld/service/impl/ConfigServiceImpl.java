package com.demo.helloworld.service.impl;

import com.demo.helloworld.service.ConfigService;
import com.demo.helloworld.vo.AppConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 应用配置 Service 实现
 *
 * 业务规则：从配置文件读取应用配置返回
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Value("${helloworld.config.app-name:HelloWorld}")
    private String appName;

    @Value("${helloworld.config.theme:light}")
    private String theme;

    @Value("${helloworld.config.language:zh_CN}")
    private String language;

    @Override
    public AppConfigVO getConfig() {
        try {
            return new AppConfigVO(appName, theme, language);
        } catch (Exception e) {
            log.warn("读取应用配置异常，返回默认值", e);
            return new AppConfigVO("HelloWorld", "light", "zh_CN");
        }
    }
}
