package com.antdigital.staff.cost.controller;

import com.antdigital.staff.common.model.ApiResponse;
import com.antdigital.staff.cost.dto.CostBudgetDTO;
import com.antdigital.staff.cost.dto.CostQueryDTO;
import com.antdigital.staff.cost.service.StaffCostService;
import com.antdigital.staff.cost.vo.CostBudgetPageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 成本预算 oneapi 接口（W09-W10）。
 */
@RestController
@RequestMapping("/api/staff/cost/budget")
public class StaffCostController {

    private final StaffCostService costService;

    @Autowired
    public StaffCostController(StaffCostService costService) {
        this.costService = costService;
    }

    /** W09 记录员工成本预算。 */
    @PostMapping
    public ApiResponse<Map<String, Long>> recordBudget(@Valid @RequestBody CostBudgetDTO dto) {
        Long budgetId = costService.recordBudget(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("budgetId", budgetId);
        return ApiResponse.success(data);
    }

    /** W10 分页/汇总查询成本预算。 */
    @PostMapping("/list")
    public ApiResponse<CostBudgetPageVO> queryBudget(@RequestBody CostQueryDTO query) {
        return ApiResponse.success(costService.queryBudget(query));
    }
}