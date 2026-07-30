package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.service.HealthService;
import com.demo.helloworld.vo.HealthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 健康检查 Controller
 * 接口 W05: GET /api/health
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {

    @Resource
    private HealthService healthService;

    /**
     * W05 健康检查
     *
     * @return 统一返回结果
     */
    @GetMapping("/health")
    public Result<HealthVO> checkHealth() {
        HealthVO vo = healthService.checkHealth();
        return Result.success(vo);
    }
}
