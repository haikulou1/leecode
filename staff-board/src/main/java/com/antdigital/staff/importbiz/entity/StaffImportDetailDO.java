package com.antdigital.staff.importbiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导入明细数据对象（staff_import_detail）。
 */
@Data
@TableName("staff_import_detail")
public class StaffImportDetailDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private Long taskId;

    private Integer rowNo;

    private String empNo;

    private String name;

    private String rawData;

    private Integer status;

    private String errorMsg;

    private LocalDateTime gmtCreate;
}