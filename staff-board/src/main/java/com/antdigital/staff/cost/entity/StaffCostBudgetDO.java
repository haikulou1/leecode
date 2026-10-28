package com.antdigital.staff.cost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工成本预算数据对象（staff_cost_budget）。
 */
@Data
@TableName("staff_cost_budget")
public class StaffCostBudgetDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private Long employeeId;

    private String empNo;

    private Integer year;

    private Integer month;

    private String costType;

    private BigDecimal amount;

    private String currency;

    private String remark;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}