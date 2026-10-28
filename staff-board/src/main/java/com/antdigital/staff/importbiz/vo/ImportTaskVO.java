package com.antdigital.staff.importbiz.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 导入任务结果（W08）。
 */
@Data
public class ImportTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Integer status;

    private Integer total;

    private Integer success;

    private Integer fail;

    private String errorFileUrl;
}