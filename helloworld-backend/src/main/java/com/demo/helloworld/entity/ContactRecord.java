package com.demo.helloworld.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 联系记录实体
 * 对应表：contact_record
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
public class ContactRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 系统自增主键 */
    private Long id;

    /** 提交者姓名 */
    private String name;

    /** 提交者邮箱 */
    private String email;

    /** 留言内容 */
    private String message;

    /** 处理状态：PENDING/RESOLVED/CLOSED */
    private String status;

    /** 是否删除：0-否 1-是 */
    private Integer isDeleted;

    /** 创建时间 */
    private Date gmtCreate;

    /** 修改时间 */
    private Date gmtModified;
}
