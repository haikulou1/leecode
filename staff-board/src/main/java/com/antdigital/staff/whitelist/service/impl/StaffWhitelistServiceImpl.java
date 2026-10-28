package com.antdigital.staff.whitelist.service.impl;

import com.antdigital.staff.common.context.StaffContextHolder;
import com.antdigital.staff.common.enums.StaffErrorCode;
import com.antdigital.staff.common.enums.WhitelistStatusEnum;
import com.antdigital.staff.common.exception.BusinessException;
import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.whitelist.dto.WhitelistDTO;
import com.antdigital.staff.whitelist.dto.WhitelistQueryDTO;
import com.antdigital.staff.whitelist.entity.StaffWhitelistDO;
import com.antdigital.staff.whitelist.mapper.StaffWhitelistMapper;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import com.antdigital.staff.whitelist.vo.StaffWhitelistVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 白名单业务服务实现。
 */
@Service
public class StaffWhitelistServiceImpl implements StaffWhitelistService {

    private final StaffWhitelistMapper whitelistMapper;

    @Autowired
    public StaffWhitelistServiceImpl(StaffWhitelistMapper whitelistMapper) {
        this.whitelistMapper = whitelistMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(WhitelistDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEmpNo()) || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(StaffErrorCode.PARAM_INVALID);
        }
        String tenantId = StaffContextHolder.getTenantId();
        StaffWhitelistDO existing = getByEmpNo(tenantId, dto.getEmpNo());
        if (existing != null) {
            if (WhitelistStatusEnum.ENABLED.getCode().equals(existing.getStatus())) {
                throw new BusinessException(StaffErrorCode.WL_EMP_EXISTS);
            }
            StaffWhitelistDO update = new StaffWhitelistDO();
            update.setId(existing.getId());
            update.setStatus(WhitelistStatusEnum.ENABLED.getCode());
            update.setReason(dto.getReason());
            whitelistMapper.updateById(update);
            return existing.getId();
        }
        StaffWhitelistDO entity = new StaffWhitelistDO();
        entity.setTenantId(tenantId);
        entity.setEmpNo(dto.getEmpNo());
        entity.setName(dto.getName());
        entity.setReason(dto.getReason());
        entity.setStatus(WhitelistStatusEnum.ENABLED.getCode());
        whitelistMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long whitelistId) {
        StaffWhitelistDO existing = whitelistMapper.selectById(whitelistId);
        if (existing == null) {
            return;
        }
        StaffWhitelistDO update = new StaffWhitelistDO();
        update.setId(existing.getId());
        update.setStatus(WhitelistStatusEnum.DISABLED.getCode());
        whitelistMapper.updateById(update);
    }

    @Override
    public PageResult<StaffWhitelistVO> pageQuery(WhitelistQueryDTO query) {
        String tenantId = StaffContextHolder.getTenantId();
        LambdaQueryWrapper<StaffWhitelistDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffWhitelistDO::getTenantId, tenantId);
        wrapper.like(StringUtils.hasText(query.getEmpNo()), StaffWhitelistDO::getEmpNo, query.getEmpNo());
        wrapper.like(StringUtils.hasText(query.getName()), StaffWhitelistDO::getName, query.getName());
        wrapper.eq(query.getStatus() != null, StaffWhitelistDO::getStatus, query.getStatus());
        wrapper.orderByDesc(StaffWhitelistDO::getGmtCreate);

        Page<StaffWhitelistDO> page = new Page<>(query.normalizedPageNum(), query.normalizedPageSize());
        Page<StaffWhitelistDO> result = whitelistMapper.selectPage(page, wrapper);
        List<StaffWhitelistVO> records = new ArrayList<>();
        for (StaffWhitelistDO item : result.getRecords()) {
            StaffWhitelistVO vo = new StaffWhitelistVO();
            vo.setWhitelistId(item.getId());
            vo.setEmpNo(item.getEmpNo());
            vo.setName(item.getName());
            vo.setReason(item.getReason());
            vo.setStatus(item.getStatus());
            vo.setGmtCreate(item.getGmtCreate());
            vo.setGmtModified(item.getGmtModified());
            records.add(vo);
        }
        return PageResult.of(result.getTotal(), records);
    }

    @Override
    public boolean checkInWhitelist(String empNo) {
        if (!StringUtils.hasText(empNo)) {
            return false;
        }
        String tenantId = StaffContextHolder.getTenantId();
        LambdaQueryWrapper<StaffWhitelistDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffWhitelistDO::getTenantId, tenantId);
        wrapper.eq(StaffWhitelistDO::getEmpNo, empNo);
        wrapper.eq(StaffWhitelistDO::getStatus, WhitelistStatusEnum.ENABLED.getCode());
        return whitelistMapper.selectCount(wrapper) > 0;
    }

    private StaffWhitelistDO getByEmpNo(String tenantId, String empNo) {
        LambdaQueryWrapper<StaffWhitelistDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffWhitelistDO::getTenantId, tenantId);
        wrapper.eq(StaffWhitelistDO::getEmpNo, empNo);
        return whitelistMapper.selectOne(wrapper);
    }
}