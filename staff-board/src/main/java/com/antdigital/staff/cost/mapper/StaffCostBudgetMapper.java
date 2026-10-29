package com.antdigital.staff.cost.mapper;

import com.antdigital.staff.cost.dto.CostQueryDTO;
import com.antdigital.staff.cost.entity.StaffCostBudgetDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 成本预算 Mapper。
 */
@Mapper
public interface StaffCostBudgetMapper extends BaseMapper<StaffCostBudgetDO> {

    /** SQL 聚合汇总金额（design W10/G4.3），避免应用层全量拉取。 */
    @Select("<script>"
            + "SELECT COALESCE(SUM(amount), 0) FROM staff_cost_budget WHERE tenant_id = #{tenantId}"
            + "<if test='query.employeeId != null'> AND employee_id = #{query.employeeId}</if>"
            + "<if test='query.empNo != null and query.empNo != \"\"'> AND emp_no = #{query.empNo}</if>"
            + "<if test='query.year != null'> AND year = #{query.year}</if>"
            + "<if test='query.month != null'> AND month = #{query.month}</if>"
            + "<if test='query.costType != null and query.costType != \"\"'> AND cost_type = #{query.costType}</if>"
            + "</script>")
    BigDecimal selectSumAmount(@Param("tenantId") String tenantId, @Param("query") CostQueryDTO query);
}