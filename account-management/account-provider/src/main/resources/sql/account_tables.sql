-- ========================================
-- 账户管理服务 - 数据库表结构
-- ========================================

-- 1. 用户账户表 (care_platform_user)
-- 存储所有用户的基本信息
CREATE TABLE IF NOT EXISTS `care_platform_user` (
    `account_id` BIGINT NOT NULL COMMENT '账户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（登录用）',
    `nickname` VARCHAR(50) NULL COMMENT '昵称/真实姓名',
    `gender` TINYINT NULL DEFAULT 0 COMMENT '性别 (0-未知, 1-男, 2-女)',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `phone` VARCHAR(500) NULL COMMENT '手机号（AES加密存储）',
    `phone_hash` VARCHAR(64) NULL COMMENT '手机号SHA256哈希值（用于登录查询）',
    `email` VARCHAR(500) NULL COMMENT '邮箱（AES加密存储）',
    `avatar_url` VARCHAR(500) NULL COMMENT '头像URL',
    `address` VARCHAR(200) NULL COMMENT '地址',
    `birthday` DATE NULL COMMENT '生日',
    `id_card` VARCHAR(500) NULL COMMENT '身份证号（AES加密存储）',
    `id_card_hash` VARCHAR(64) NULL COMMENT '身份证号SHA256哈希值（用于登录查询）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `dele` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0-未删除, 1-已删除)',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `ext` JSON NULL COMMENT '扩展字段',
    PRIMARY KEY (`account_id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone_hash` (`phone_hash`),
    UNIQUE KEY `uk_id_card_hash` (`id_card_hash`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_dele` (`dele`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';

-- 2. 患者信息表 (patient)
-- 存储患者专属信息
CREATE TABLE IF NOT EXISTS `patient` (
    `id` BIGINT NOT NULL COMMENT '患者ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID（关联care_platform_user表）',
    `attending_doctor_id` BIGINT NULL COMMENT '主治医生ID（关联doctor表）',
    `medical_history` TEXT NULL COMMENT '病史摘要',
    `allergies` VARCHAR(500) NULL COMMENT '过敏史',
    `blood_type` VARCHAR(10) NULL COMMENT '血型 (A/B/AB/O)',
    `height` INT NULL COMMENT '身高（cm）',
    `weight` INT NULL COMMENT '体重（kg）',
    `emergency_contact` VARCHAR(50) NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(500) NULL COMMENT '紧急联系人电话（AES加密存储）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `dele` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0-未删除, 1-已删除)',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `ext` JSON NULL COMMENT '扩展字段（如既往病史详情、用药记录等）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_id` (`account_id`),
    KEY `idx_attending_doctor` (`attending_doctor_id`),
    KEY `idx_dele` (`dele`),
    CONSTRAINT `fk_patient_account` FOREIGN KEY (`account_id`) REFERENCES `care_platform_user` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者信息表';

-- 3. 医生信息表 (doctor)
-- 存储医生专属信息
CREATE TABLE IF NOT EXISTS `doctor` (
    `id` BIGINT NOT NULL COMMENT '医生ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID（关联care_platform_user表）',
    `department` VARCHAR(50) NULL COMMENT '科室',
    `title` VARCHAR(50) NULL COMMENT '职称（如主治医师、主任医师等）',
    `experience` INT NULL COMMENT '工作经验（年）',
    `certification_number` VARCHAR(500) NULL COMMENT '执业证书编号（AES加密存储）',
    `education` VARCHAR(50) NULL COMMENT '学历',
    `bio` TEXT NULL COMMENT '个人简介',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `dele` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0-未删除, 1-已删除)',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    `ext` JSON NULL COMMENT '扩展字段（如擅长领域、出诊时间等）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_id` (`account_id`),
    KEY `idx_department` (`department`),
    KEY `idx_dele` (`dele`),
    CONSTRAINT `fk_doctor_account` FOREIGN KEY (`account_id`) REFERENCES `care_platform_user` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生信息表';

-- ========================================
-- 索引优化说明
-- ========================================
-- 1. care_platform_user表：
--    - uk_username: 保证用户名唯一性，用于登录查询
--    - uk_phone_hash: 手机号哈希唯一索引，用于手机号登录查询
--    - uk_id_card_hash: 身份证号哈希唯一索引，用于身份证号登录查询
--    - idx_dele: 优化逻辑删除查询
--    注意: phone、email、id_card字段已加密存储，不再用于唯一性约束
--
-- 2. patient表：
--    - uk_account_id: 一个账户只能对应一个患者身份
--    - idx_attending_doctor: 优化按医生查询患者列表
--
-- 3. doctor表：
--    - uk_account_id: 一个账户只能对应一个医生身份
--    - idx_department: 优化按科室查询医生列表
--    注意: certification_number已加密存储，移除了唯一索引

-- ========================================
-- 数据示例
-- ========================================

-- 示例：插入一个患者账户
-- INSERT INTO care_platform_user (account_id, username, nickname, gender, password, phone, email)
-- VALUES (1001, 'patient001', '张三', 1, '$2a$10$...', '13800138000', 'zhangsan@example.com');

-- INSERT INTO patient (id, account_id, attending_doctor_id, blood_type, height, weight)
-- VALUES (2001, 1001, 3001, 'A', 175, 70);

-- 示例：插入一个医生账户
-- INSERT INTO care_platform_user (account_id, username, nickname, gender, password, phone, email)
-- VALUES (1002, 'doctor001', '李医生', 1, '$2a$10$...', '13900139000', 'lidoctor@example.com');

-- INSERT INTO doctor (id, account_id, department, title, experience, certification_number)
-- VALUES (3001, 1002, '内科', '主治医师', 8, 'CERT-2024-001');
