package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.service.VersionService;
import com.demo.helloworld.vo.VersionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 版本信息 Controller
 * 接口 W07: GET /api/version
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class VersionController {

    @Resource
    private VersionService versionService;

    /**
     * W07 获取版本信息
     *
     * @return 统一返回结果
     */
    @GetMapping("/version")
    public Result<VersionVO> getVersion() {
        log.info("获取版本信息请求");
        VersionVO vo = versionService.getVersion();
        return Result.success(vo);
    }
}
