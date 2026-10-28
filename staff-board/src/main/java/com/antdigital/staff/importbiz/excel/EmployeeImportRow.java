package com.antdigital.staff.importbiz.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工批量导入 Excel 行模型（EasyExcel 流式读取，与模板表头一一对应）。
 */
@Data
public class EmployeeImportRow {

    @ExcelProperty(value = "工号")
    private String empNo;

    @ExcelProperty(value = "姓名")
    private String name;

    @ExcelProperty(value = "部门")
    private String deptName;

    @ExcelProperty(value = "岗位")
    private String position;

    @ExcelProperty(value = "入职日期")
    private String hireDate;

    @ExcelProperty(value = "邮箱")
    private String email;

    @ExcelProperty(value = "手机号")
    private String phone;

    @ExcelProperty(value = "成本类型")
    private String costType;

    @ExcelProperty(value = "预算年度")
    private Integer costYear;

    @ExcelProperty(value = "预算月份")
    private Integer costMonth;

    @ExcelProperty(value = "预算金额")
    private BigDecimal costAmount;
}