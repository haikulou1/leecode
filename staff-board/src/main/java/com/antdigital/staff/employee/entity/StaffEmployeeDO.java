package com.antdigital.staff.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工基本信息数据对象（staff_employee）。
 */
@Data
@TableName("staff_employee")
public class StaffEmployeeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String empNo;

    private String name;

    private Long deptId;

    private String deptName;

    private String position;

    private LocalDate hireDate;

    private String email;

    private String phone;

    private String idCardNo;

    private Integer status;

    private String source;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}