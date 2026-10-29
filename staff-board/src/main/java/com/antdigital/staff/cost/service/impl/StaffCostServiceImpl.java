package com.antdigital.staff.cost.service.impl;

import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.cost.dto.CostBudgetDTO;
import com.antdigital.staff.cost.dto.CostQueryDTO;
import com.antdigital.staff.cost.entity.StaffCostBudgetDO;
import com.antdigital.staff.cost.mapper.StaffCostBudgetMapper;
import com.antdigital.staff.cost.service.StaffCostService;
import com.antdigital.staff.cost.vo.CostBudgetPageVO;
import com.antdigital.staff.cost.vo.StaffCostBudgetVO;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 成本预算业务服务实现。
 */
@Service
public class StaffCostServiceImpl implements StaffCostService {

    private static final int PRICE_SCALE = 2;
    private static final int MAX_PRECISION = 14;
    private static final String DEFAULT_CURRENCY = "CNY";

    private final StaffCostBudgetMapper costBudgetMapper;
    private final StaffEmployeeService employeeService;

    @Autowired
    public StaffCostServiceImpl(StaffCostBudgetMapper costBudgetMapper,
                                StaffEmployeeService employeeService) {
        this.costBudgetMapper = costBudgetMapper;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordBudget(CostBudgetDTO dto) {
        if (dto == null || dto.getEmployeeId() == null || dto.getYear() == null || dto.getMonth() == null
                || !StringUtils.hasText(dto.getCostType())) {
            throw new BusinessException(StaffErrorCode.PARAM_INVALID);
        }
        validateAmount(dto.getAmount());
        StaffEmployeeDO employee = employeeService.getEntityById(dto.getEmployeeId());
        if (employee == null) {
            throw new BusinessException(StaffErrorCode.COST_EMP_NOT_FOUND);
        }
        String tenantId = StaffContextHolder.getTenantId();
        StaffCostBudgetDO existing = findByDimension(tenantId, dto.getEmployeeId(), dto.getYear(),
                dto.getMonth(), dto.getCostType());
        if (existing != null) {
            StaffCostBudgetDO update = new StaffCostBudgetDO();
            update.setId(existing.getId());
            update.setAmount(dto.getAmount());
            update.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : DEFAULT_CURRENCY);
            update.setRemark(dto.getRemark());
            costBudgetMapper.updateById(update);
            return existing.getId();
        }
        StaffCostBudgetDO entity = new StaffCostBudgetDO();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setEmpNo(employee.getEmpNo());
        entity.setYear(dto.getYear());
        entity.setMonth(dto.getMonth());
        entity.setCostType(dto.getCostType());
        entity.setAmount(dto.getAmount());
        entity.setCurrency(StringUtils.hasText(dto.getCurrency()) ? dto.getCurrency() : DEFAULT_CURRENCY);
        entity.setRemark(dto.getRemark());
        costBudgetMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public CostBudgetPageVO queryBudget(CostQueryDTO query) {
        String tenantId = StaffContextHolder.getTenantId();
        LambdaQueryWrapper<StaffCostBudgetDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffCostBudgetDO::getTenantId, tenantId);
        wrapper.eq(query.getEmployeeId() != null, StaffCostBudgetDO::getEmployeeId, query.getEmployeeId());
        wrapper.eq(StringUtils.hasText(query.getEmpNo()), StaffCostBudgetDO::getEmpNo, query.getEmpNo());
        wrapper.eq(query.getYear() != null, StaffCostBudgetDO::getYear, query.getYear());
        wrapper.eq(query.getMonth() != null, StaffCostBudgetDO::getMonth, query.getMonth());
        wrapper.eq(StringUtils.hasText(query.getCostType()), StaffCostBudgetDO::getCostType, query.getCostType());
        wrapper.orderByDesc(StaffCostBudgetDO::getGmtCreate);

        boolean aggregate = Boolean.TRUE.equals(query.getAggregate());
        BigDecimal summary = null;
        if (aggregate) {
            summary = sumAmount(tenantId, query);
        }

        Page<StaffCostBudgetDO> page = new Page<>(query.normalizedPageNum(), query.normalizedPageSize());
        Page<StaffCostBudgetDO> result = costBudgetMapper.selectPage(page, wrapper);
        List<StaffCostBudgetVO> records = new ArrayList<>();
        for (StaffCostBudgetDO item : result.getRecords()) {
            StaffCostBudgetVO vo = new StaffCostBudgetVO();
            vo.setBudgetId(item.getId());
            vo.setEmployeeId(item.getEmployeeId());
            vo.setEmpNo(item.getEmpNo());
            vo.setYear(item.getYear());
            vo.setMonth(item.getMonth());
            vo.setCostType(item.getCostType());
            vo.setAmount(item.getAmount());
            vo.setCurrency(item.getCurrency());
            vo.setRemark(item.getRemark());
            vo.setGmtCreate(item.getGmtCreate());
            records.add(vo);
        }
        PageResult<StaffCostBudgetVO> pageResult = PageResult.of(result.getTotal(), records);
        return CostBudgetPageVO.from(pageResult, summary);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(StaffErrorCode.COST_AMOUNT_INVALID);
        }
        if (amount.scale() > PRICE_SCALE || amount.precision() > MAX_PRECISION) {
            throw new BusinessException(StaffErrorCode.COST_AMOUNT_INVALID);
        }
    }

    private StaffCostBudgetDO findByDimension(String tenantId, Long employeeId, Integer year, Integer month,
                                              String costType) {
        LambdaQueryWrapper<StaffCostBudgetDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffCostBudgetDO::getTenantId, tenantId);
        wrapper.eq(StaffCostBudgetDO::getEmployeeId, employeeId);
        wrapper.eq(StaffCostBudgetDO::getYear, year);
        wrapper.eq(StaffCostBudgetDO::getMonth, month);
        wrapper.eq(StaffCostBudgetDO::getCostType, costType);
        return costBudgetMapper.selectOne(wrapper);
    }

    private BigDecimal sumAmount(String tenantId, CostQueryDTO query) {
        BigDecimal sum = costBudgetMapper.selectSumAmount(tenantId, query);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}