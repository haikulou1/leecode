package com.antdigital.staff.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果：{@code { total, records }}。空结果返回空数组，避免前端空指针判断。
 *
 * @param <T> 记录类型
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数。 */
    private Long total;

    /** 当前页记录。 */
    private List<T> records;

    public PageResult() {
        this.total = 0L;
        this.records = new ArrayList<>();
    }

    public PageResult(Long total, List<T> records) {
        this.total = total != null ? total : 0L;
        this.records = records != null ? records : new ArrayList<>();
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, new ArrayList<>());
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "PageResult{total=" + total + ", records=" + records + '}';
    }
}