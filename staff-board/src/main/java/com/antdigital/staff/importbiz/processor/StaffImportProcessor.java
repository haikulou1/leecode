package com.antdigital.staff.importbiz.processor;

import com.antdigital.staff.common.constant.StaffConstants;
import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.cost.dto.CostBudgetDTO;
import com.antdigital.staff.cost.service.StaffCostService;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.antdigital.staff.importbiz.config.ImportProperties;
import com.antdigital.staff.importbiz.excel.EmployeeImportRow;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 导入单行处理器：白名单准入校验 → 员工 upsert → 成本预算记录，返回失败原因（null 表示成功）。
 * 独立成组件便于单元测试核心规则。
 */
@Component
public class StaffImportProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StaffImportProcessor.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final StaffWhitelistService whitelistService;
    private final StaffEmployeeService employeeService;
    private final StaffCostService costService;
    private final ImportProperties importProperties;

    @Autowired
    public StaffImportProcessor(StaffWhitelistService whitelistService,
                                StaffEmployeeService employeeService,
                                StaffCostService costService,
                                ImportProperties importProperties) {
        this.whitelistService = whitelistService;
        this.employeeService = employeeService;
        this.costService = costService;
        this.importProperties = importProperties;
    }

    /**
     * 处理单行。返回 null 表示成功，否则返回失败原因。
     */
    public String processRow(EmployeeImportRow row) {
        if (row == null || !StringUtils.hasText(row.getEmpNo()) || !StringUtils.hasText(row.getName())) {
            return "工号/姓名不能为空";
        }
        if (importProperties.isWhitelistEnforced() && !whitelistService.checkInWhitelist(row.getEmpNo())) {
            return "该工号未在白名单内";
        }

        StaffEmployeeDO employee = new StaffEmployeeDO();
        employee.setTenantId(StaffContextHolder.getTenantId());
        employee.setEmpNo(row.getEmpNo());
        employee.setName(row.getName());
        employee.setDeptName(row.getDeptName());
        employee.setPosition(row.getPosition());
        employee.setHireDate(parseHireDate(row.getHireDate()));
        employee.setEmail(row.getEmail());
        employee.setPhone(row.getPhone());
        employee.setIdCardNo(null);
        employee.setStatus(1);
        employee.setSource(StaffConstants.SOURCE_IMPORT);
        employeeService.saveOrUpdateEntity(employee);

        if (StringUtils.hasText(row.getCostType()) && row.getCostAmount() != null) {
            StaffEmployeeDO saved = employeeService.getEntityByEmpNo(row.getEmpNo());
            if (saved == null || saved.getId() == null) {
                String msg = "成本预算写入失败: 员工不存在 empNo=" + row.getEmpNo();
                LOG.error(msg);
                return msg;
            }
            try {
                CostBudgetDTO budgetDto = new CostBudgetDTO();
                budgetDto.setEmployeeId(saved.getId());
                budgetDto.setYear(row.getCostYear());
                budgetDto.setMonth(row.getCostMonth());
                budgetDto.setCostType(row.getCostType());
                budgetDto.setAmount(row.getCostAmount());
                costService.recordBudget(budgetDto);
            } catch (Exception ex) {
                // 预算行失败仅影响本行预算，不影响员工信息落库（遵循设计 R05）
                LOG.error("成本预算写入失败 empNo:{} employeeId:{}", row.getEmpNo(), saved.getId(), ex);
                return "成本预算写入失败: " + ex.getMessage();
            }
        }
        return null;
    }

    /** 解析入职日期字符串（yyyy-MM-dd），失败返回 null 不阻断。 */
    public LocalDate parseHireDate(String hireDate) {
        if (!StringUtils.hasText(hireDate)) {
            return null;
        }
        try {
            return LocalDate.parse(hireDate, DATE_FORMATTER);
        } catch (Exception ex) {
            LOG.warn("入职日期解析失败，按空处理 hireDate:{}", hireDate);
            return null;
        }
    }
}