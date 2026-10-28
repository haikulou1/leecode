package com.antdigital.staff.cost.vo;

import com.antdigital.staff.common.model.PageResult;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 成本预算分页/汇总结果（W10），在分页基础上携带汇总金额。
 */
@Data
public class CostBudgetPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private List<StaffCostBudgetVO> records;

    /** 汇总金额（aggregate=true 时返回）。 */
    private BigDecimal summaryAmount;

    public CostBudgetPageVO() {
        this.total = 0L;
        this.records = new ArrayList<>();
        this.summaryAmount = BigDecimal.ZERO;
    }

    public static CostBudgetPageVO from(PageResult<StaffCostBudgetVO> page, BigDecimal summaryAmount) {
        CostBudgetPageVO vo = new CostBudgetPageVO();
        vo.setTotal(page.getTotal());
        vo.setRecords(page.getRecords());
        vo.setSummaryAmount(summaryAmount != null ? summaryAmount : BigDecimal.ZERO);
        return vo;
    }
}