package com.demo.helloworld.service;

import com.demo.helloworld.vo.HelloVO;

/**
 * 欢迎语 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface HelloService {

    /**
     * 获取欢迎语
     *
     * @param language 语言标识，默认 zh_CN
     * @return 欢迎语 VO
     */
    HelloVO getHello(String language);
}
