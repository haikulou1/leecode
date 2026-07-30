package com.demo.helloworld.service;

import com.demo.helloworld.dto.ContactSubmitReq;
import com.demo.helloworld.vo.ContactVO;

/**
 * 联系 Service
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface ContactService {

    /**
     * 获取联系信息
     *
     * @return 联系信息 VO
     */
    ContactVO getContact();

    /**
     * 提交联系表单
     *
     * @param req 提交请求
     * @return 联系记录 ID
     */
    Long submitContact(ContactSubmitReq req);
}
