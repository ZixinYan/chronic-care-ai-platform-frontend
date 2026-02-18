-- =============================================
-- 站内信系统数据库表结构
-- Author: zixin
-- Create Date: 2024
-- =============================================

-- 1. 消息主表
CREATE TABLE IF NOT EXISTS `message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `message_type` INT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-系统消息 2-个人消息 3-群发消息',
    `sender_id` BIGINT DEFAULT 0 COMMENT '发送者ID (0表示系统)',
    `sender_name` VARCHAR(50) COMMENT '发送者姓名',
    `receiver_id` BIGINT COMMENT '接收者ID (群发消息时为NULL)',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT COMMENT '消息内容',
    `status` INT NOT NULL DEFAULT 1 COMMENT '消息状态: 1-未读 2-已读 3-已删除 4-已撤回',
    `is_broadcast` TINYINT NOT NULL DEFAULT 0 COMMENT '是否群发: 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `read_time` DATETIME COMMENT '阅读时间',
    `revoke_time` DATETIME COMMENT '撤回时间',
    PRIMARY KEY (`message_id`),
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息主表';

-- 2. 消息接收者关联表 (用于群发消息)
CREATE TABLE IF NOT EXISTS `message_recipient` (
    `recipient_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '接收者记录ID',
    `message_id` BIGINT NOT NULL COMMENT '消息ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '消息状态: 1-未读 2-已读 3-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `read_time` DATETIME COMMENT '阅读时间',
    PRIMARY KEY (`recipient_id`),
    UNIQUE KEY `uk_message_receiver` (`message_id`, `receiver_id`),
    INDEX `idx_message_id` (`message_id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息接收者关联表';

-- 3. 消息模板表 (可选扩展功能)
CREATE TABLE IF NOT EXISTS `message_template` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `message_type` INT NOT NULL DEFAULT 1 COMMENT '消息类型',
    `title_template` VARCHAR(200) NOT NULL COMMENT '标题模板 (支持占位符)',
    `content_template` TEXT NOT NULL COMMENT '内容模板 (支持占位符)',
    `params` VARCHAR(500) COMMENT '参数列表 (JSON格式)',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `remark` VARCHAR(500) COMMENT '备注说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`template_id`),
    UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 插入默认消息模板
INSERT INTO `message_template` (`template_code`, `template_name`, `message_type`, `title_template`, `content_template`, `params`, `remark`)
VALUES 
('HEALTH_ALERT', '健康预警通知', 1, '健康预警：{indicator}异常', '尊敬的{userName}，您的{indicator}数值为{value}，{alertLevel}。建议：{suggestion}', '["userName","indicator","value","alertLevel","suggestion"]', '健康数据异常预警'),
('APPOINTMENT_REMIND', '预约提醒', 1, '预约提醒：{doctorName}医生', '您好{userName}，您预约了{doctorName}医生，就诊时间：{appointmentTime}，就诊地址：{location}', '["userName","doctorName","appointmentTime","location"]', '医生预约提醒'),
('REPORT_READY', '报告已就绪', 1, '您的{reportType}报告已生成', '您好{userName}，您的{reportType}报告已生成，请及时查看。', '["userName","reportType"]', '医学报告生成通知'),
('MEDICINE_REMIND', '服药提醒', 1, '服药提醒：{medicineName}', '请按时服用{medicineName}，剂量：{dosage}，时间：{time}', '["medicineName","dosage","time"]', '按时服药提醒'),
('FAMILY_BIND', '家属绑定通知', 1, '家属绑定成功', '{userName}已成功绑定您为家属，您可以查看其健康数据。', '["userName"]', '家属绑定成功通知');
