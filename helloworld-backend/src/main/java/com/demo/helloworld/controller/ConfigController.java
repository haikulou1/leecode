package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.service.ConfigService;
import com.demo.helloworld.vo.AppConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 应用配置 Controller
 * 接口 W06: GET /api/config
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ConfigController {

    @Resource
    private ConfigService configService;

    /**
     * W06 获取应用配置
     *
     * @return 统一返回结果
     */
    @GetMapping("/config")
    public Result<AppConfigVO> getConfig() {
        log.info("获取应用配置请求");
        AppConfigVO vo = configService.getConfig();
        return Result.success(vo);
    }
}
