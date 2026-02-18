-- 医生服务数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `chronic_care_doctor` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `chronic_care_doctor`;

-- 医生信息表
CREATE TABLE IF NOT EXISTS `doctor` (
  `id` BIGINT(20) NOT NULL COMMENT '医生ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID，关联users表',
  `department` VARCHAR(100) DEFAULT NULL COMMENT '科室',
  `title` VARCHAR(50) DEFAULT NULL COMMENT '职称（如主治医师、主任医师等）',
  `experience` INT(11) DEFAULT NULL COMMENT '工作经验（年数）',
  `certification_number` VARCHAR(100) DEFAULT NULL COMMENT '执业证书编号',
  `education` VARCHAR(50) DEFAULT NULL COMMENT '学历',
  `bio` TEXT DEFAULT NULL COMMENT '简介',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `dele` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记（0-未删除，1-已删除）',
  `version` INT(11) DEFAULT 0 COMMENT '版本号（乐观锁）',
  `ext` JSON DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_department` (`department`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';

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

-- 示例数据：医生信息
INSERT INTO `doctor` (`id`, `user_id`, `department`, `title`, `experience`, `certification_number`, `education`, `bio`) VALUES
(1, 1001, '心血管内科', '主任医师', 15, 'CERT2023001', '医学博士', '擅长心血管疾病诊治，有丰富的临床经验'),
(2, 1002, '内分泌科', '主治医师', 8, 'CERT2023002', '医学硕士', '专注于糖尿病、甲状腺疾病的诊疗');

-- 示例数据：日程安排
INSERT INTO `doctor_schedule` (`id`, `doctor_id`, `patient_id`, `schedule`, `schedule_category`, `schedule_day`, `priority`, `status`) VALUES
(1, 1, 2001, '复诊患者李先生，高血压随访', 1, '2024-01-15', 2, 'PENDING'),
(2, 1, 2002, '张女士心脏彩超检查', 1, '2024-01-15', 3, 'IN_PROGRESS'),
(3, 1, NULL, '下午病房查房', 3, '2024-01-15', 2, 'PENDING'),
(4, 2, 2003, '王先生糖尿病初诊', 1, '2024-01-15', 3, 'PENDING');
