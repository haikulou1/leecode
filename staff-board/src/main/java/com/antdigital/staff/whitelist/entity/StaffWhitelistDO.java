package com.antdigital.staff.whitelist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 白名单数据对象（staff_whitelist）。
 */
@Data
@TableName("staff_whitelist")
public class StaffWhitelistDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String empNo;

    private String name;

    private String reason;

    private Integer status;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}