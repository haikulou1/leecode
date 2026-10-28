package com.antdigital.staff.cost.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工成本预算记录入参（W09）。
 */
@Data
public class CostBudgetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 员工ID（必填）。 */
    private Long employeeId;

    /** 年度（必填）。 */
    private Integer year;

    /** 月份（1-12，0 表示全年，必填）。 */
    private Integer month;

    /** 成本类型。 */
    private String costType;

    /** 金额。 */
    private BigDecimal amount;

    /** 币种，默认 CNY。 */
    private String currency;

    /** 备注。 */
    private String remark;
}