package com.antdigital.staff.employee.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工明细/列表展示对象（敏感字段脱敏后返回）。
 */
@Data
public class StaffEmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long employeeId;

    private String empNo;

    private String name;

    private Long deptId;

    private String deptName;

    private String position;

    private LocalDate hireDate;

    private String email;

    /** 手机号（脱敏）。 */
    private String phone;

    /** 身份证号（脱敏）。 */
    private String idCardNo;

    private Integer status;

    private String source;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}