package com.demo.helloworld.service;

import com.demo.helloworld.vo.VersionVO;

/**
 * 版本信息 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface VersionService {

    /**
     * 获取版本信息
     *
     * @return 版本信息 VO
     */
    VersionVO getVersion();
}
