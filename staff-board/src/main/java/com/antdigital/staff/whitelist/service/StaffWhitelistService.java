package com.antdigital.staff.whitelist.service;

import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.whitelist.dto.WhitelistDTO;
import com.antdigital.staff.whitelist.dto.WhitelistQueryDTO;
import com.antdigital.staff.whitelist.vo.StaffWhitelistVO;

/**
 * 白名单业务服务。
 */
public interface StaffWhitelistService {

    /** 新增白名单（同租户工号唯一，已停用则复启用）。 */
    Long add(WhitelistDTO dto);

    /** 移除白名单（逻辑停用，保留历史）。 */
    void remove(Long whitelistId);

    /** 分页查询白名单。 */
    PageResult<StaffWhitelistVO> pageQuery(WhitelistQueryDTO query);

    /** 白名单准入校验：工号在启用状态白名单内返回 true。 */
    boolean checkInWhitelist(String empNo);
}