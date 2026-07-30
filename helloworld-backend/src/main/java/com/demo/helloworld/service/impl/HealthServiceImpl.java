package com.demo.helloworld.service.impl;

import com.demo.helloworld.enums.HealthStatusEnum;
import com.demo.helloworld.mapper.ContactMapper;
import com.demo.helloworld.service.HealthService;
import com.demo.helloworld.vo.HealthVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 健康检查 Service 实现
 *
 * 业务规则：
 * R07 - 执行 SELECT 1 探测数据库连通性，连通返回 UP，异常返回 DOWN
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class HealthServiceImpl implements HealthService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Resource
    private ContactMapper contactMapper;

    @Override
    public HealthVO checkHealth() {
        String status;
        try {
            // R07: SELECT 探测数据库连通性
            contactMapper.checkConnection(1);
            status = HealthStatusEnum.UP.getCode();
        } catch (Exception e) {
            log.error("健康检查失败，数据库连接异常", e);
            status = HealthStatusEnum.DOWN.getCode();
        }
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return new HealthVO(status, timestamp);
    }
}
