-- 角色和权限配置说明
-- 
-- 本系统采用RBAC (Role-Based Access Control) 权限模型
-- 用户(Account) -> 用户角色(AccountRole) -> 角色(Role) -> 角色权限(RolePermission) -> 权限(Permission)
--
-- 角色代码说明:
--   1 - ADMIN (管理员) - 拥有系统所有权限
--   2 - DOCTOR (医生) - 拥有医生工作台、患者管理等权限
--   3 - PATIENT (患者) - 拥有个人健康档案、预约等权限
--
-- 权限代码说明:
--   1001 - 用户管理
--   1002 - 角色管理
--   2001 - 医生工作台访问
--   2002 - 患者信息查看
--   2003 - 诊断记录管理
--   3001 - 个人健康档案
--   3002 - 预约挂号
--   3003 - 查看诊断报告

-- ================================
-- 用户角色关系表
-- ================================
CREATE TABLE IF NOT EXISTS `account_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `role_code` INT NOT NULL COMMENT '角色代码',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`, `role_code`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- ================================
-- 角色表
-- ================================
CREATE TABLE IF NOT EXISTS `role` (
  `role_code` INT NOT NULL COMMENT '角色代码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) COMMENT '角色描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================
-- 权限表
-- ================================
CREATE TABLE IF NOT EXISTS `permission` (
  `permission_code` INT NOT NULL COMMENT '权限代码',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(200) COMMENT '权限描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ================================
-- 角色权限关系表
-- ================================
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` INT NOT NULL COMMENT '角色代码',
  `permission_code` INT NOT NULL COMMENT '权限代码',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_code`, `permission_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系表';

-- ================================
-- 初始化角色数据
-- ================================
INSERT INTO `role` (`role_code`, `role_name`, `description`) VALUES
(1, 'ADMIN', '系统管理员，拥有所有权限'),
(2, 'DOCTOR', '医生，可以访问医生工作台和患者信息'),
(3, 'PATIENT', '患者，可以管理个人健康信息和预约')
ON DUPLICATE KEY UPDATE `role_name`=VALUES(`role_name`), `description`=VALUES(`description`);

-- ================================
-- 初始化权限数据
-- ================================
INSERT INTO `permission` (`permission_code`, `permission_name`, `description`) VALUES
-- 管理员权限
(1001, 'USER_MANAGEMENT', '用户管理权限'),
(1002, 'ROLE_MANAGEMENT', '角色管理权限'),
(1003, 'SYSTEM_CONFIG', '系统配置权限'),

-- 医生权限
(2001, 'DOCTOR_WORKBENCH', '医生工作台访问权限'),
(2002, 'PATIENT_INFO_VIEW', '查看患者信息权限'),
(2003, 'DIAGNOSIS_RECORD', '诊断记录管理权限'),
(2004, 'SCHEDULE_MANAGEMENT', '日程管理权限'),

-- 患者权限
(3001, 'PERSONAL_HEALTH', '个人健康档案权限'),
(3002, 'APPOINTMENT', '预约挂号权限'),
(3003, 'VIEW_DIAGNOSIS_REPORT', '查看诊断报告权限'),
(3004, 'HEALTH_MONITORING', '健康监测权限')
ON DUPLICATE KEY UPDATE `permission_name`=VALUES(`permission_name`), `description`=VALUES(`description`);

-- ================================
-- 初始化角色权限关系
-- ================================

-- 管理员拥有所有权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
(1, 1001), (1, 1002), (1, 1003),
(1, 2001), (1, 2002), (1, 2003), (1, 2004),
(1, 3001), (1, 3002), (1, 3003), (1, 3004)
ON DUPLICATE KEY UPDATE `role_code`=VALUES(`role_code`);

-- 医生权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
(2, 2001), -- 医生工作台
(2, 2002), -- 查看患者信息
(2, 2003), -- 诊断记录管理
(2, 2004)  -- 日程管理
ON DUPLICATE KEY UPDATE `role_code`=VALUES(`role_code`);

-- 患者权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
(3, 3001), -- 个人健康档案
(3, 3002), -- 预约挂号
(3, 3003), -- 查看诊断报告
(3, 3004)  -- 健康监测
ON DUPLICATE KEY UPDATE `role_code`=VALUES(`role_code`);
