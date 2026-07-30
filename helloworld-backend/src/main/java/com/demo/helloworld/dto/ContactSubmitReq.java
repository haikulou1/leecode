package com.demo.helloworld.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 提交联系表单请求 DTO
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
public class ContactSubmitReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 提交者姓名，1-64字符 */
    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 64, message = "姓名长度需在1-64字符之间")
    private String name;

    /** 提交者邮箱，需符合邮箱格式 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不合法")
    @Size(max = 128, message = "邮箱长度不能超过128字符")
    private String email;

    /** 留言内容，1-512字符 */
    @NotBlank(message = "留言内容不能为空")
    @Size(min = 1, max = 512, message = "留言内容长度需在1-512字符之间")
    private String message;
}
