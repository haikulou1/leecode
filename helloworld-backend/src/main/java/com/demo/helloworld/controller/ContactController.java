package com.demo.helloworld.controller;

import com.demo.helloworld.common.Result;
import com.demo.helloworld.dto.ContactSubmitReq;
import com.demo.helloworld.service.ContactService;
import com.demo.helloworld.vo.ContactVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 联系 Controller
 * 接口 W03: GET  /api/contact
 * 接口 W04: POST /api/contact/submit
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Resource
    private ContactService contactService;

    /**
     * W03 获取联系信息
     *
     * @return 统一返回结果
     */
    @GetMapping
    public Result<ContactVO> getContact() {
        log.info("获取联系信息请求");
        ContactVO vo = contactService.getContact();
        return Result.success(vo);
    }

    /**
     * W04 提交联系表单
     *
     * @param req 提交请求
     * @return 统一返回结果，data.id 为联系记录 ID
     */
    @PostMapping("/submit")
    public Result<Map<String, Long>> submitContact(@Valid @RequestBody ContactSubmitReq req) {
        log.info("提交联系表单请求，name={}", req.getName());
        Long id = contactService.submitContact(req);
        Map<String, Long> data = new HashMap<>(1);
        data.put("id", id);
        return Result.success(data);
    }
}
