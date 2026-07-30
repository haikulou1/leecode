package com.demo.helloworld.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 欢迎语配置实体
 * 对应表：hello_message
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
public class HelloMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 系统自增主键 */
    private Long id;

    /** 欢迎语文案 */
    private String message;

    /** 语言标识 */
    private String language;

    /** 是否删除：0-否 1-是 */
    private Integer isDeleted;

    /** 创建时间 */
    private Date gmtCreate;

    /** 修改时间 */
    private Date gmtModified;
}
