package com.antdigital.staff.common.model;

import com.antdigital.staff.common.constant.StaffConstants;

/**
 * 通用分页入参。页码小于 1 归一到 1，页大小超上限归一到上限。
 */
public class PageQuery {

    private Integer pageNum;

    private Integer pageSize;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /** 归一化后的页码，最小为 1。 */
    public int normalizedPageNum() {
        return pageNum == null || pageNum < 1 ? StaffConstants.DEFAULT_PAGE_NUM : pageNum;
    }

    /** 归一化后的页大小，最小 1，最大 {@link StaffConstants#MAX_PAGE_SIZE}。 */
    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return StaffConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, StaffConstants.MAX_PAGE_SIZE);
    }

    /** 计算 SQL offset，避免溢出。 */
    public long offset() {
        return (long) (normalizedPageNum() - 1) * normalizedPageSize();
    }
}