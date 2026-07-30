package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.service.HelloService;
import com.demo.helloworld.vo.HelloVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 欢迎语 Controller
 * 接口 W01: GET /api/hello
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HelloController {

    @Resource
    private HelloService helloService;

    /**
     * W01 获取欢迎语
     *
     * @param language 语言标识，默认 zh_CN
     * @return 统一返回结果
     */
    @GetMapping("/hello")
    public Result<HelloVO> getHello(@RequestParam(value = "language", required = false) String language) {
        log.info("获取欢迎语请求，language={}", language);
        HelloVO vo = helloService.getHello(language);
        return Result.success(vo);
    }
}
