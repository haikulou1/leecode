package com.antdigital.staff.whitelist.dto;

import com.antdigital.staff.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 白名单分页查询入参（W12）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WhitelistQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String empNo;

    private String name;

    private Integer status;
}