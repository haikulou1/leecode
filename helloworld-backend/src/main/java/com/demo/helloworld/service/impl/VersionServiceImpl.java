package com.demo.helloworld.service.impl;

import com.demo.helloworld.service.VersionService;
import com.demo.helloworld.vo.VersionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 版本信息 Service 实现
 *
 * 业务规则：从配置文件读取版本号和构建时间
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class VersionServiceImpl implements VersionService {

    @Value("${helloworld.version.version:1.0.0}")
    private String version;

    @Value("${helloworld.version.build-time:2026-07-30T10:00:00}")
    private String buildTime;

    @Override
    public VersionVO getVersion() {
        try {
            return new VersionVO(version, buildTime);
        } catch (Exception e) {
            log.warn("读取版本信息异常，返回默认值", e);
            return new VersionVO("1.0.0", "2026-07-30T10:00:00");
        }
    }
}
