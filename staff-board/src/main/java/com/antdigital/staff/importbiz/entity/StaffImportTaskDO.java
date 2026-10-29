package com.antdigital.staff.importbiz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导入任务数据对象（staff_import_task）。
 */
@Data
@TableName("staff_import_task")
public class StaffImportTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tenantId;

    private String fileName;

    private String fileUrl;

    /** 文件 MD5（与 tenant_id 组合构成任务级幂等键，design §5.3.3.1）。 */
    private String fileMd5;

    private Integer total;

    private Integer success;

    private Integer fail;

    private Integer status;

    private String errorFileUrl;

    private String operator;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}