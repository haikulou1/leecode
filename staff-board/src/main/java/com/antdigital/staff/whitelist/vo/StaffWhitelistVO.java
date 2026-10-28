package com.antdigital.staff.whitelist.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 白名单展示对象。
 */
@Data
public class StaffWhitelistVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long whitelistId;

    private String empNo;

    private String name;

    private String reason;

    private Integer status;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}