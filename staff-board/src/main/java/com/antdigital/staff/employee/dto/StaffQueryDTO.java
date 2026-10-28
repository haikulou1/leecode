package com.antdigital.staff.employee.dto;

import com.antdigital.staff.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工分页查询入参（W05）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffQueryDTO extends PageQuery {

    private static final long serialVersionUID = 1L;

    /** 工号模糊匹配。 */
    private String empNo;

    /** 姓名模糊匹配。 */
    private String name;

    /** 部门ID。 */
    private Long deptId;

    /** 状态：1-在职，0-离职。 */
    private Integer status;
}