package com.antdigital.staff.cost.service;

import com.antdigital.staff.cost.dto.CostBudgetDTO;
import com.antdigital.staff.cost.dto.CostQueryDTO;
import com.antdigital.staff.cost.vo.CostBudgetPageVO;

/**
 * 成本预算业务服务。
 */
public interface StaffCostService {

    /** 记录员工成本预算（同员工+期间+类型幂等覆盖）。 */
    Long recordBudget(CostBudgetDTO dto);

    /** 分页/汇总查询成本预算，aggregate=true 时返回汇总金额。 */
    CostBudgetPageVO queryBudget(CostQueryDTO query);
}