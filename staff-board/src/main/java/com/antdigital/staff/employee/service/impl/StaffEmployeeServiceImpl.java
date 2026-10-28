package com.antdigital.staff.employee.service.impl;

import com.antdigital.staff.common.constant.StaffConstants;
import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.EmployeeStatusEnum;
import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.common.util.AesCryptUtil;
import com.antdigital.staff.common.util.MaskUtil;
import com.antdigital.staff.employee.dto.StaffEmployeeDTO;
import com.antdigital.staff.employee.dto.StaffQueryDTO;
import com.antdigital.staff.employee.entity.StaffEmployeeDO;
import com.antdigital.staff.employee.mapper.StaffEmployeeMapper;
import com.antdigital.staff.employee.service.StaffEmployeeService;
import com.antdigital.staff.employee.vo.StaffEmployeeVO;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工信息业务服务实现。
 */
@Service
public class StaffEmployeeServiceImpl implements StaffEmployeeService {

    private final StaffEmployeeMapper employeeMapper;
    private final StaffWhitelistService whitelistService;
    private final AesCryptUtil aesCryptUtil;

    @Autowired
    public StaffEmployeeServiceImpl(StaffEmployeeMapper employeeMapper,
                                    StaffWhitelistService whitelistService,
                                    AesCryptUtil aesCryptUtil) {
        this.employeeMapper = employeeMapper;
        this.whitelistService = whitelistService;
        this.aesCryptUtil = aesCryptUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StaffEmployeeDTO dto) {
        validateRequired(dto);
        if (!whitelistService.checkInWhitelist(dto.getEmpNo())) {
            throw new BusinessException(StaffErrorCode.WL_NOT_IN_WHITELIST);
        }
        StaffEmployeeDO existing = getEntityByEmpNo(dto.getEmpNo());
        if (existing != null) {
            throw new BusinessException(StaffErrorCode.EMP_NO_EXISTS);
        }
        StaffEmployeeDO entity = new StaffEmployeeDO();
        entity.setTenantId(StaffContextHolder.getTenantId());
        entity.setEmpNo(dto.getEmpNo());
        copyToEntity(dto, entity);
        entity.setStatus(EmployeeStatusEnum.NORMAL.getCode());
        entity.setSource(StaffConstants.SOURCE_MANUAL);
        entity.setIdCardNo(aesCryptUtil.encrypt(dto.getIdCardNo()));
        employeeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long employeeId, StaffEmployeeDTO dto) {
        StaffEmployeeDO existing = getEntityById(employeeId);
        if (existing == null || !EmployeeStatusEnum.NORMAL.getCode().equals(existing.getStatus())) {
            throw new BusinessException(StaffErrorCode.EMP_NOT_FOUND);
        }
        copyToEntity(dto, existing);
        if (StringUtils.hasText(dto.getIdCardNo())) {
            existing.setIdCardNo(aesCryptUtil.encrypt(dto.getIdCardNo()));
        }
        employeeMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long employeeId) {
        StaffEmployeeDO existing = getEntityById(employeeId);
        if (existing == null) {
            return;
        }
        if (EmployeeStatusEnum.LEAVE.getCode().equals(existing.getStatus())) {
            // 幂等：已删除不再重复删除
            return;
        }
        StaffEmployeeDO update = new StaffEmployeeDO();
        update.setId(existing.getId());
        update.setStatus(EmployeeStatusEnum.LEAVE.getCode());
        employeeMapper.updateById(update);
    }

    @Override
    public StaffEmployeeVO getDetail(Long employeeId) {
        StaffEmployeeDO entity = getEntityById(employeeId);
        if (entity == null) {
            throw new BusinessException(StaffErrorCode.EMP_NOT_FOUND);
        }
        return toVO(entity);
    }

    @Override
    public PageResult<StaffEmployeeVO> pageQuery(StaffQueryDTO query) {
        String tenantId = StaffContextHolder.getTenantId();
        LambdaQueryWrapper<StaffEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEmployeeDO::getTenantId, tenantId);
        wrapper.like(StringUtils.hasText(query.getEmpNo()), StaffEmployeeDO::getEmpNo, query.getEmpNo());
        wrapper.like(StringUtils.hasText(query.getName()), StaffEmployeeDO::getName, query.getName());
        wrapper.eq(query.getDeptId() != null, StaffEmployeeDO::getDeptId, query.getDeptId());
        wrapper.eq(query.getStatus() != null, StaffEmployeeDO::getStatus, query.getStatus());
        wrapper.orderByDesc(StaffEmployeeDO::getGmtCreate);

        Page<StaffEmployeeDO> page = new Page<>(query.normalizedPageNum(), query.normalizedPageSize());
        Page<StaffEmployeeDO> result = employeeMapper.selectPage(page, wrapper);
        List<StaffEmployeeVO> records = new ArrayList<>();
        for (StaffEmployeeDO item : result.getRecords()) {
            records.add(toVO(item));
        }
        return PageResult.of(result.getTotal(), records);
    }

    @Override
    public StaffEmployeeDO getEntityById(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        LambdaQueryWrapper<StaffEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEmployeeDO::getId, employeeId);
        wrapper.eq(StaffEmployeeDO::getTenantId, StaffContextHolder.getTenantId());
        return employeeMapper.selectOne(wrapper);
    }

    @Override
    public StaffEmployeeDO getEntityByEmpNo(String empNo) {
        if (!StringUtils.hasText(empNo)) {
            return null;
        }
        LambdaQueryWrapper<StaffEmployeeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEmployeeDO::getEmpNo, empNo);
        wrapper.eq(StaffEmployeeDO::getTenantId, StaffContextHolder.getTenantId());
        return employeeMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateEntity(StaffEmployeeDO entity) {
        StaffEmployeeDO existing = getEntityByEmpNo(entity.getEmpNo());
        if (existing != null) {
            entity.setId(existing.getId());
            entity.setGmtCreate(existing.getGmtCreate());
            employeeMapper.updateById(entity);
            return existing.getId();
        }
        entity.setId(null);
        employeeMapper.insert(entity);
        return entity.getId();
    }

    private void validateRequired(StaffEmployeeDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEmpNo()) || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(StaffErrorCode.EMP_PARAM_INVALID);
        }
    }

    private void copyToEntity(StaffEmployeeDTO dto, StaffEmployeeDO entity) {
        entity.setName(dto.getName());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptName());
        entity.setPosition(dto.getPosition());
        entity.setHireDate(dto.getHireDate());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
    }

    private StaffEmployeeVO toVO(StaffEmployeeDO entity) {
        StaffEmployeeVO vo = new StaffEmployeeVO();
        vo.setEmployeeId(entity.getId());
        vo.setEmpNo(entity.getEmpNo());
        vo.setName(entity.getName());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setPosition(entity.getPosition());
        vo.setHireDate(entity.getHireDate());
        vo.setEmail(entity.getEmail());
        vo.setPhone(MaskUtil.maskPhone(entity.getPhone()));
        vo.setIdCardNo(MaskUtil.maskIdCard(aesCryptUtil.decrypt(entity.getIdCardNo())));
        vo.setStatus(entity.getStatus());
        vo.setSource(entity.getSource());
        vo.setGmtCreate(entity.getGmtCreate());
        vo.setGmtModified(entity.getGmtModified());
        return vo;
    }
}