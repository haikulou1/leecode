package com.antdigital.staff.whitelist.controller;

import com.antdigital.staff.common.model.ApiResponse;
import com.antdigital.staff.common.model.PageResult;
import com.antdigital.staff.whitelist.dto.WhitelistDTO;
import com.antdigital.staff.whitelist.dto.WhitelistQueryDTO;
import com.antdigital.staff.whitelist.service.StaffWhitelistService;
import com.antdigital.staff.whitelist.vo.StaffWhitelistVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 白名单 oneapi 接口（W11-W13）。
 */
@RestController
@RequestMapping("/api/staff/whitelist")
public class StaffWhitelistController {

    private final StaffWhitelistService whitelistService;

    @Autowired
    public StaffWhitelistController(StaffWhitelistService whitelistService) {
        this.whitelistService = whitelistService;
    }

    /** W11 新增白名单。 */
    @PostMapping
    public ApiResponse<Map<String, Long>> add(@Valid @RequestBody WhitelistDTO dto) {
        Long whitelistId = whitelistService.add(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("whitelistId", whitelistId);
        return ApiResponse.success(data);
    }

    /** W12 分页查询白名单。 */
    @PostMapping("/list")
    public ApiResponse<PageResult<StaffWhitelistVO>> pageQuery(@RequestBody WhitelistQueryDTO query) {
        return ApiResponse.success(whitelistService.pageQuery(query));
    }

    /** W13 移除白名单。 */
    @DeleteMapping("/{whitelistId}")
    public ApiResponse<Void> remove(@PathVariable Long whitelistId) {
        whitelistService.remove(whitelistId);
        return ApiResponse.success(null);
    }
}