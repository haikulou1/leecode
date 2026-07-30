-- ============================================================
-- HelloWorld 数据库初始化脚本
-- 执行前请先创建数据库：CREATE DATABASE IF NOT EXISTS helloworld DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- ============================================================

USE helloworld;

-- ------------------------------------------------------------
-- 表1：hello_message 欢迎语配置表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `hello_message`;
CREATE TABLE `hello_message` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    `message`      VARCHAR(255) NOT NULL COMMENT '欢迎语文案',
    `language`     VARCHAR(16)  NOT NULL DEFAULT 'zh_CN' COMMENT '语言标识',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    `gmt_create`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hello_message_lang` (`language`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='欢迎语配置表';

-- 初始化数据
INSERT INTO `hello_message` (`message`, `language`, `is_deleted`) VALUES
    ('你好，世界！', 'zh_CN', 0),
    ('Hello, World!', 'en_US', 0);

-- ------------------------------------------------------------
-- 表2：contact_record 联系记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `contact_record`;
CREATE TABLE `contact_record` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '系统自增主键',
    `name`         VARCHAR(64)  NOT NULL COMMENT '提交者姓名',
    `email`        VARCHAR(128) NOT NULL COMMENT '提交者邮箱',
    `message`      VARCHAR(512) NOT NULL COMMENT '留言内容',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING/RESOLVED/CLOSED',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    `gmt_create`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_contact_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系记录表';
