package com.demo.helloworld.service;

import com.demo.helloworld.vo.AboutVO;

/**
 * 关于信息 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface AboutService {

    /**
     * 获取关于信息
     *
     * @return 关于信息 VO
     */
    AboutVO getAbout();
}
