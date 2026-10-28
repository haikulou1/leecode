-- 人员看板 MySQL DDL（全新表，前缀命名 staff_）
-- 规范：表名小写、必备字段 id/gmt_create/gmt_modified、金额用 decimal、禁外键、唯一索引保证幂等。

CREATE TABLE IF NOT EXISTS staff_employee (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户ID',
    emp_no       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '工号',
    name         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '姓名',
    dept_id      BIGINT       NULL COMMENT '部门ID（可选）',
    dept_name    VARCHAR(128) NULL COMMENT '部门名称（冗余）',
    position     VARCHAR(64)  NULL COMMENT '岗位',
    hire_date    DATE         NULL COMMENT '入职日期',
    email        VARCHAR(128) NULL COMMENT '邮箱',
    phone        VARCHAR(32)  NULL COMMENT '手机号（脱敏展示）',
    id_card_no   VARCHAR(256) NULL COMMENT '身份证号（加密存储）',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-在职/正常，0-离职/停用',
    source       VARCHAR(16)  NOT NULL DEFAULT 'MANUAL' COMMENT '来源：MANUAL-手动录入，IMPORT-导入',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_emp_no (tenant_id, emp_no),
    KEY idx_tenant_name (tenant_id, name),
    KEY idx_tenant_status (tenant_id, status),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工基本信息表';

CREATE TABLE IF NOT EXISTS staff_whitelist (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户ID',
    emp_no       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '工号',
    name         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '姓名（冗余，便于展示）',
    reason       VARCHAR(255) NULL COMMENT '加入原因',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_emp_no (tenant_id, emp_no),
    KEY idx_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工准入白名单表';

CREATE TABLE IF NOT EXISTS staff_cost_budget (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '租户ID',
    employee_id  BIGINT        NOT NULL COMMENT '员工ID（关联 staff_employee.id）',
    emp_no       VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '工号（冗余）',
    year         SMALLINT      NOT NULL COMMENT '预算年度',
    month        TINYINT       NOT NULL DEFAULT 0 COMMENT '预算月份（1-12，0 表示全年）',
    cost_type    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '成本类型',
    amount       DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    currency     CHAR(3)       NOT NULL DEFAULT 'CNY' COMMENT '币种',
    remark       VARCHAR(255)  NULL COMMENT '备注',
    gmt_create   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_emp_year_month_type (employee_id, year, month, cost_type),
    KEY idx_tenant_year_month (tenant_id, year, month),
    KEY idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工成本预算表';

CREATE TABLE IF NOT EXISTS staff_import_task (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    tenant_id      VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户ID',
    file_name      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原始文件名',
    file_url       VARCHAR(512) NULL COMMENT '导入文件对象存储地址',
    total          INT          NOT NULL DEFAULT 0 COMMENT '总行数',
    success        INT          NOT NULL DEFAULT 0 COMMENT '成功行数',
    fail           INT          NOT NULL DEFAULT 0 COMMENT '失败行数',
    status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-成功，3-部分成功，4-失败',
    error_file_url VARCHAR(512) NULL COMMENT '错误报告文件地址',
    operator       VARCHAR(64)  NULL COMMENT '操作人',
    gmt_create     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_tenant_status (tenant_id, status),
    KEY idx_tenant_create (tenant_id, gmt_create)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入任务表';

CREATE TABLE IF NOT EXISTS staff_import_detail (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    tenant_id    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '租户ID',
    task_id      BIGINT       NOT NULL COMMENT '导入任务ID',
    row_no       INT          NOT NULL DEFAULT 0 COMMENT '行号',
    emp_no       VARCHAR(32)  NULL COMMENT '工号',
    name         VARCHAR(64)  NULL COMMENT '姓名',
    raw_data     TEXT         NULL COMMENT '原始行数据（JSON）',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '处理状态：1-成功，0-失败',
    error_msg    VARCHAR(512) NULL COMMENT '失败原因',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入明细表';