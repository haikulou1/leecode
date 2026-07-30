package com.demo.helloworld.service;

import com.demo.helloworld.vo.HealthVO;

/**
 * 健康检查 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface HealthService {

    /**
     * 检查服务健康状态
     *
     * @return 健康状态 VO
     */
    HealthVO checkHealth();
}
