-- ============================================
-- 站内信系统数据库脚本
-- ============================================

-- 消息表
CREATE TABLE IF NOT EXISTS `message_inbox` (
    `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
    `message_type` INT(11) NOT NULL DEFAULT 2 COMMENT '消息类型: 1-系统通知, 2-个人消息, 3-群发消息, 4-公告',
    `sender_id` BIGINT(20) DEFAULT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '发送者名称',
    `receiver_id` BIGINT(20) DEFAULT NULL COMMENT '接收者ID(个人消息时使用,群发消息时为null)',
    `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '接收者名称',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT COMMENT '消息内容',
    `status` INT(11) NOT NULL DEFAULT 0 COMMENT '消息状态: 0-未读, 1-已读, 2-已撤回, 3-已删除',
    `is_broadcast` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否群发: 0-否, 1-是',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `revoke_time` DATETIME DEFAULT NULL COMMENT '撤回时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`message_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';

-- 群发消息接收者表
CREATE TABLE IF NOT EXISTS `message_recipient` (
    `id` BIGINT(20) NOT NULL COMMENT '主键ID',
    `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
    `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
    `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '接收者名称',
    `status` INT(11) NOT NULL DEFAULT 0 COMMENT '消息状态: 0-未读, 1-已读, 3-已删除',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群发消息接收者表';
