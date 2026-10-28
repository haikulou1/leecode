package com.antdigital.staff.employee.mapper;

import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工信息 Mapper（MyBatis-Plus，参数化查询，无字符串拼接 SQL）。
 */
@Mapper
public interface StaffEmployeeMapper extends BaseMapper<StaffEmployeeDO> {
}