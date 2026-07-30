package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 联系信息返回 VO
 * 对应接口：W03 GET /api/contact
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 联系邮箱 */
    private String email;

    /** 联系电话 */
    private String phone;

    /** 联系地址 */
    private String address;
}
