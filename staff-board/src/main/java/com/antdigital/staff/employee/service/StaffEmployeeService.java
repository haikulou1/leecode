package com.antdigital.staff.employee.service;

import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.employee.dto.StaffEmployeeDTO;
import com.antdigital.staff.employee.dto.StaffQueryDTO;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.vo.StaffEmployeeVO;

/**
 * 员工信息业务服务。
 */
public interface StaffEmployeeService {

    /** 录入员工基本信息（白名单准入 + 工号唯一 + 敏感字段加密）。 */
    Long create(StaffEmployeeDTO dto);

    /** 修改员工基本信息（工号不可变更）。 */
    void update(Long employeeId, StaffEmployeeDTO dto);

    /** 逻辑删除（状态置离职/停用，幂等）。 */
    void delete(Long employeeId);

    /** 查询员工详情（敏感字段脱敏）。 */
    StaffEmployeeVO getDetail(Long employeeId);

    /** 分页查询员工列表。 */
    PageResult<StaffEmployeeVO> pageQuery(StaffQueryDTO query);

    /** 根据 ID 查询实体（内部模块使用），可能返回 null。 */
    StaffEmployeeDO getEntityById(Long employeeId);

    /** 根据租户 + 工号查询实体（内部模块使用），可能返回 null。 */
    StaffEmployeeDO getEntityByEmpNo(String empNo);

    /** 保存或更新实体（导入模块使用，返回员工 ID）。 */
    Long saveOrUpdateEntity(StaffEmployeeDO entity);
}