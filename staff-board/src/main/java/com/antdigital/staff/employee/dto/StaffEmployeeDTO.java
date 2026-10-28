package com.antdigital.staff.employee.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工录入/修改请求对象（W01/W02）。
 */
@Data
public class StaffEmployeeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工号（录入必填，修改不可变）。 */
    private String empNo;

    /** 姓名。 */
    private String name;

    private Long deptId;

    private String deptName;

    private String position;

    private LocalDate hireDate;

    private String email;

    private String phone;

    private String idCardNo;
}