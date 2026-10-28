package com.antdigital.staff.whitelist.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 白名单新增入参（W11）。
 */
@Data
public class WhitelistDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工号。 */
    private String empNo;

    /** 姓名。 */
    private String name;

    /** 加入原因。 */
    private String reason;
}