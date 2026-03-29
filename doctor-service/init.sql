-- 医生服务数据库初始化脚本

-- 创建数据库
USE `chronic-care-ai-platform`;

-- 医生日程表
CREATE TABLE IF NOT EXISTS `doctor_schedule` (
  `id` BIGINT(20) NOT NULL COMMENT '日程ID',
  `doctor_id` BIGINT(20) NOT NULL COMMENT '医生ID',
  `patient_id` BIGINT(20) DEFAULT NULL COMMENT '患者ID',
  `schedule` VARCHAR(500) NOT NULL COMMENT '日程内容/描述',
  `schedule_category` INT(11) DEFAULT NULL COMMENT '日程类别ID（关联schedule_category表）',
  `schedule_day` VARCHAR(20) NOT NULL COMMENT '日程日期（格式：YYYY-MM-DD）',
  `priority` INT(11) DEFAULT 2 COMMENT '优先级（1-低，2-中，3-高，4-紧急）',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '日程状态（PENDING-待处理，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消）',
  `result` TEXT DEFAULT NULL COMMENT '执行结果/诊断报告',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT(11) DEFAULT 0 COMMENT '版本号（乐观锁）',
  `ext` JSON DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_schedule_day` (`schedule_day`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`schedule_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生日程表';

-- 日程类别表
CREATE TABLE IF NOT EXISTS `schedule_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '类别ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '类别名称（如：门诊、手术、查房、会诊）',
  `category_alias` VARCHAR(50) DEFAULT NULL COMMENT '类别别名/简称',
  `describe` VARCHAR(200) DEFAULT NULL COMMENT '类别描述',
  `ext` JSON DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程类别表';

-- 初始化日程类别数据
INSERT INTO `schedule_category` (`category_name`, `category_alias`, `describe`) VALUES
('门诊', 'OUTPATIENT', '门诊接诊患者'),
('手术', 'SURGERY', '手术安排'),
('查房', 'WARD_ROUND', '病房巡查'),
('会诊', 'CONSULTATION', '多科室会诊'),
('急诊', 'EMERGENCY', '急诊处理'),
('教学', 'TEACHING', '教学培训'),
('科研', 'RESEARCH', '科研工作'),
('行政', 'ADMINISTRATION', '行政事务');