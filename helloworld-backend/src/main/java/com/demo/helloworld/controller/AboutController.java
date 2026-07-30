package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.service.AboutService;
import com.demo.helloworld.vo.AboutVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 关于信息 Controller
 * 接口 W02: GET /api/about
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AboutController {

    @Resource
    private AboutService aboutService;

    /**
     * W02 获取关于信息
     *
     * @return 统一返回结果
     */
    @GetMapping("/about")
    public Result<AboutVO> getAbout() {
        log.info("获取关于信息请求");
        AboutVO vo = aboutService.getAbout();
        return Result.success(vo);
    }
}
