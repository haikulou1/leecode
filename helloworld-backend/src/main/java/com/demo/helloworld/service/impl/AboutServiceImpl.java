package com.demo.helloworld.service.impl;

import com.demo.helloworld.service.AboutService;
import com.demo.helloworld.vo.AboutVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 关于信息 Service 实现
 *
 * 业务规则：
 * R03 - 关于信息从配置文件读取，无数据库查询，配置缺失时返回默认值
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class AboutServiceImpl implements AboutService {

    @Value("${helloworld.about.title:关于 HelloWorld}")
    private String title;

    @Value("${helloworld.about.content:这是一个前后端分离的 HelloWorld 示例应用。}")
    private String content;

    @Value("${helloworld.about.author:DTCoder}")
    private String author;

    @Override
    public AboutVO getAbout() {
        // R03: 从配置文件读取，配置缺失时由 @Value 默认值兜底
        try {
            return new AboutVO(title, content, author);
        } catch (Exception e) {
            log.warn("读取关于信息配置异常，返回默认值", e);
            return new AboutVO("关于 HelloWorld", "这是一个前后端分离的 HelloWorld 示例应用。", "DTCoder");
        }
    }
}
