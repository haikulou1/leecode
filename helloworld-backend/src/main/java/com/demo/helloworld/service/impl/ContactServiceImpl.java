package com.demo.helloworld.service.impl;

import com.demo.helloworld.common.BizException;
import com.demo.helloworld.common.ResultCode;
import com.demo.helloworld.dto.ContactSubmitReq;
import com.demo.helloworld.entity.ContactRecord;
import com.demo.helloworld.enums.ContactStatusEnum;
import com.demo.helloworld.enums.IsDeletedEnum;
import com.demo.helloworld.mapper.ContactMapper;
import com.demo.helloworld.service.ContactService;
import com.demo.helloworld.vo.ContactVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 联系 Service 实现
 *
 * 业务规则：
 * R04 - name 非空且 1-64 字符（由 @Valid 校验，此处二次校验）
 * R05 - email 非空且符合邮箱正则（由 @Valid 校验，此处二次校验）
 * R06 - message 非空且 1-512 字符（由 @Valid 校验，此处二次校验）
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Slf4j
@Service
public class ContactServiceImpl implements ContactService {

    @Value("${helloworld.contact.email:contact@helloworld.demo}")
    private String email;

    @Value("${helloworld.contact.phone:123-4567-8900}")
    private String phone;

    @Value("${helloworld.contact.address:杭州市}")
    private String address;

    @Resource
    private ContactMapper contactMapper;

    @Override
    public ContactVO getContact() {
        return new ContactVO(email, phone, address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitContact(ContactSubmitReq req) {
        // R04: name 二次校验
        validateName(req.getName());
        // R05: email 二次校验
        validateEmail(req.getEmail());
        // R06: message 二次校验
        validateMessage(req.getMessage());

        // 构建实体
        ContactRecord record = new ContactRecord();
        record.setName(req.getName());
        record.setEmail(req.getEmail());
        record.setMessage(req.getMessage());
        record.setStatus(ContactStatusEnum.PENDING.getCode());
        record.setIsDeleted(IsDeletedEnum.NOT_DELETED.getValue());

        // 插入并获取自增主键
        contactMapper.insert(record);
        log.info("联系表单提交成功，recordId={}", record.getId());
        return record.getId();
    }

    /**
     * 校验姓名
     * R04: name 非空且 1-64 字符
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty() || name.length() > 64) {
            throw new BizException(ResultCode.CONTACT_002);
        }
    }

    /**
     * 校验邮箱
     * R05: email 非空且符合邮箱正则
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BizException(ResultCode.CONTACT_003);
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new BizException(ResultCode.CONTACT_003);
        }
    }

    /**
     * 校验留言内容
     * R06: message 非空且 1-512 字符
     */
    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty() || message.length() > 512) {
            throw new BizException(ResultCode.CONTACT_004);
        }
    }
}
