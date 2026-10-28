package com.antdigital.staff.cost.dto;

import com.antdigital.staff.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成本预算查询入参（W10）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CostQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    private Long employeeId;

    private String empNo;

    private Integer year;

    private Integer month;

    private String costType;

    /** 为 true 时在结果中返回汇总金额 summaryAmount。 */
    private Boolean aggregate;
}