package com.demo.helloworld.service.impl;

import com.demo.helloworld.common.ResultCode;
import com.demo.helloworld.entity.HelloMessage;
import com.demo.helloworld.enums.IsDeletedEnum;
import com.demo.helloworld.enums.LanguageEnum;
import com.demo.helloworld.mapper.HelloMapper;
import com.demo.helloworld.service.HelloService;
import com.demo.helloworld.vo.HelloVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 欢迎语 Service 实现
 *
 * 业务规则：
 * R01 - 查询结果为空时返回默认文案 "Hello, World!"
 * R02 - language 参数仅允许 zh_CN/en_US
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class HelloServiceImpl implements HelloService {

    /** 查空时默认文案 */
    private static final String DEFAULT_MESSAGE = "Hello, World!";

    @Resource
    private HelloMapper helloMapper;

    @Override
    public HelloVO getHello(String language) {
        // 默认 zh_CN
        String lang = (language == null || language.trim().isEmpty())
                ? LanguageEnum.ZH_CN.getCode()
                : language;

        // R02: language 参数校验
        if (!LanguageEnum.isValid(lang)) {
            throw new com.demo.helloworld.common.BizException(ResultCode.HELLO_002);
        }

        HelloVO vo;
        try {
            // 查询未删除的欢迎语
            HelloMessage message = helloMapper.selectByLanguage(lang, IsDeletedEnum.NOT_DELETED.getValue());

            // R01: 查空时返回默认文案
            if (message == null) {
                log.info("欢迎语未配置，language={}，返回默认文案", lang);
                vo = new HelloVO(DEFAULT_MESSAGE, lang);
            } else {
                vo = new HelloVO(message.getMessage(), message.getLanguage());
            }
        } catch (Exception e) {
            // 异常场景：数据库连接异常，降级返回默认文案
            log.error("查询欢迎语异常，降级返回默认文案，language={}", lang, e);
            vo = new HelloVO(DEFAULT_MESSAGE, lang);
        }
        return vo;
    }
}
