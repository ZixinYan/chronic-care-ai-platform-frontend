-- ============================================
-- 站内信系统数据库脚本
-- ============================================

-- 消息表
CREATE TABLE IF NOT EXISTS `message_inbox` (
    `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
    `message_type` INT(11) NOT NULL DEFAULT 2 COMMENT '消息类型: 1-系统通知, 2-个人消息, 3-群发消息, 4-公告',
    `sender_id` BIGINT(20) DEFAULT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '发送者名称',
    `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
    `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '接收者名称',
    `group_message_id` BIGINT(20) DEFAULT NULL COMMENT '群发消息组ID(群发消息时使用,用于标识同一群发消息组)',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT COMMENT '消息内容',
    `status` INT(11) NOT NULL DEFAULT 0 COMMENT '消息状态: 0-未读, 1-已读, 2-已撤回, 3-已删除',
    `is_broadcast` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否群发: 0-否, 1-是',
    `read_time` BIGINT(20) DEFAULT NULL COMMENT '阅读时间(Unix毫秒时间戳)',
    `revoke_time` BIGINT(20) DEFAULT NULL COMMENT '撤回时间(Unix毫秒时间戳)',
    `create_time` BIGINT(20) NOT NULL COMMENT '创建时间(Unix毫秒时间戳)',
    `update_time` BIGINT(20) NOT NULL COMMENT '更新时间(Unix毫秒时间戳)',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`message_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_group_message_id` (`group_message_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';
