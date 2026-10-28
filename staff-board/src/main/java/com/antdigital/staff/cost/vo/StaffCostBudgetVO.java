package com.antdigital.staff.cost.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本预算展示对象。
 */
@Data
public class StaffCostBudgetVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long budgetId;

    private Long employeeId;

    private String empNo;

    private Integer year;

    private Integer month;

    private String costType;

    private BigDecimal amount;

    private String currency;

    private String remark;

    private LocalDateTime gmtCreate;
}