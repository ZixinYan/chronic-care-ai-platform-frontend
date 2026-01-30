-- =============================================
-- 简化权限体系 - 完整初始化脚本
-- 适用于慢性病AI平台
-- Author: zixin
-- Create Date: 2024
-- =============================================

-- ================================
-- 说明:
-- 本系统采用简化的RBAC权限模型
-- 用户(Account) -> 角色(Role) -> 权限(Permission)
--
-- 核心设计原则:
-- 1. 只区分读(read)和写(write)权限
-- 2. 权限按模块划分: {module}:{operation}
-- 3. 降低复杂度,易于维护
-- ================================

-- ================================
-- 1. 用户角色关系表
-- ================================
CREATE TABLE IF NOT EXISTS `account_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `role_code` VARCHAR(20) NOT NULL COMMENT '角色代码',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`, `role_code`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- ================================
-- 2. 角色表
-- ================================
CREATE TABLE IF NOT EXISTS `role` (
  `role_code` VARCHAR(20) NOT NULL COMMENT '角色代码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) COMMENT '角色描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================
-- 3. 权限表
-- ================================
CREATE TABLE IF NOT EXISTS `permission` (
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限代码',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(200) COMMENT '权限描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ================================
-- 4. 角色权限关系表
-- ================================
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` VARCHAR(20) NOT NULL COMMENT '角色代码',
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限代码',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_code`, `permission_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系表';

-- ================================
-- 5. 初始化角色数据
-- ================================
INSERT INTO `role` (`role_code`, `role_name`, `description`) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限'),
('DOCTOR', '医生', '可以访问医生工作台，查看和管理患者信息'),
('PATIENT', '患者', '可以管理个人健康信息和预约')
ON DUPLICATE KEY UPDATE 
  `role_name` = VALUES(`role_name`), 
  `description` = VALUES(`description`);

-- ================================
-- 6. 初始化权限数据 (简化版)
-- ================================
INSERT INTO `permission` (`permission_code`, `permission_name`, `description`) VALUES
-- 医生模块权限
('doctor:read', '医生读权限', '查看医生信息、日程列表、日程详情'),
('doctor:write', '医生写权限', '生成日程、完成日程、取消日程、更新状态'),

-- 患者模块权限
('patient:read', '患者读权限', '查看患者信息、健康数据'),
('patient:write', '患者写权限', '修改患者信息、上传健康数据'),

-- 管理员权限
('admin:all', '管理员全部权限', '系统管理员拥有所有权限'),

-- 消息系统权限
('message:read', '消息读权限', '查看站内信、系统通知'),
('message:write', '消息写权限', '发送消息、删除消息'),

-- 健康数据权限
('health:read', '健康数据读权限', '查看健康数据、报告'),
('health:write', '健康数据写权限', '上传健康数据、修改记录')
ON DUPLICATE KEY UPDATE 
  `permission_name` = VALUES(`permission_name`), 
  `description` = VALUES(`description`);

-- ================================
-- 7. 初始化角色权限关系
-- ================================

-- 清空现有关系 (可选，首次初始化时使用)
-- DELETE FROM `role_permission`;

-- ADMIN 拥有所有权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
('ADMIN', 'doctor:read'),
('ADMIN', 'doctor:write'),
('ADMIN', 'patient:read'),
('ADMIN', 'patient:write'),
('ADMIN', 'admin:all'),
('ADMIN', 'message:read'),
('ADMIN', 'message:write'),
('ADMIN', 'health:read'),
('ADMIN', 'health:write')
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- DOCTOR 拥有医生模块读写权限 + 患者读权限 + 消息读写权限 + 健康数据读权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
('DOCTOR', 'doctor:read'),
('DOCTOR', 'doctor:write'),
('DOCTOR', 'patient:read'),     -- 医生可以查看患者信息
('DOCTOR', 'message:read'),
('DOCTOR', 'message:write'),
('DOCTOR', 'health:read')        -- 医生可以查看健康数据
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- PATIENT 拥有患者模块读写权限 + 消息读权限 + 健康数据读写权限
INSERT INTO `role_permission` (`role_code`, `permission_code`) VALUES
('PATIENT', 'patient:read'),
('PATIENT', 'patient:write'),
('PATIENT', 'message:read'),
('PATIENT', 'health:read'),
('PATIENT', 'health:write')
ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- ================================
-- 8. 权限说明文档
-- ================================
/*
=============================================
权限设计说明
=============================================

1. 权限命名规范:
   格式: {module}:{operation}
   - module: 模块名称 (doctor, patient, message, health, admin)
   - operation: 操作类型 (read, write, all)

2. 权限分类:
   
   【医生模块】
   - doctor:read  → 查看医生信息、日程列表、日程详情
   - doctor:write → 生成日程、完成日程、取消日程、更新状态
   
   【患者模块】
   - patient:read  → 查看患者信息、健康档案
   - patient:write → 修改患者信息、绑定家属
   
   【消息系统】
   - message:read  → 查看收件箱、消息详情
   - message:write → 发送消息、删除消息
   
   【健康数据】
   - health:read  → 查看健康数据、体检报告
   - health:write → 上传健康数据、修改记录
   
   【管理员】
   - admin:all → 系统管理员全部权限

3. 角色权限矩阵:

   +----------+-------+-------+---------+---------+---------+---------+
   |   权限   | ADMIN | DOCTOR| PATIENT |   说明   |         |         |
   +----------+-------+-------+---------+---------+---------+---------+
   | doctor:  | ✅ ✅ | ✅ ✅ |         | 医生模块 | read    | write   |
   | patient: | ✅ ✅ | ✅    | ✅ ✅   | 患者模块 |         |         |
   | message: | ✅ ✅ | ✅ ✅ | ✅      | 消息系统 |         |         |
   | health:  | ✅ ✅ | ✅    | ✅ ✅   | 健康数据 |         |         |
   | admin:   | ✅    |       |         | 管理权限 |         |         |
   +----------+-------+-------+---------+---------+---------+---------+

4. 权限扩展方式:
   
   新增模块时，只需添加两个权限:
   
   INSERT INTO `permission` VALUES
   ('{module}:read', '{模块}读权限', '查看{模块}相关数据'),
   ('{module}:write', '{模块}写权限', '创建、修改、删除{模块}数据');
   
   然后为相关角色分配权限:
   
   INSERT INTO `role_permission` VALUES
   ('ROLE_CODE', '{module}:read'),
   ('ROLE_CODE', '{module}:write');

5. 使用示例:
   
   Controller层权限注解:
   
   @RestController
   @RequestMapping("/doctor/workbench")
   @RequireRole("DOCTOR")
   public class DoctorWorkbenchController {
       
       @GetMapping("/schedule/list")
       @RequirePermission("doctor:read")
       public Result querySchedule() { }
       
       @PostMapping("/schedule/complete")
       @RequirePermission("doctor:write")
       public Result completeSchedule() { }
   }

6. 权限验证流程:
   
   Gateway层:
     ↓ 验证JWT Token，提取userId, roles, authorities
   
   Consumer层:
     ↓ @RequireRole验证角色
     ↓ @RequirePermission验证权限
   
   Provider层:
     ↓ 验证数据归属 (只能操作自己的数据)

7. 数据库索引说明:
   
   - account_role: uk_account_role (account_id, role_code) 唯一索引
   - role_permission: uk_role_permission (role_code, permission_code) 唯一索引
   - 优化查询性能，防止重复数据

=============================================
*/

-- ================================
-- 9. 验证数据
-- ================================

-- 查看所有角色
SELECT * FROM `role`;

-- 查看所有权限
SELECT * FROM `permission`;

-- 查看角色权限关系
SELECT 
    r.role_name,
    r.role_code,
    p.permission_code,
    p.permission_name,
    p.description
FROM `role` r
LEFT JOIN `role_permission` rp ON r.role_code = rp.role_code
LEFT JOIN `permission` p ON rp.permission_code = p.permission_code
ORDER BY r.role_code, p.permission_code;

-- 查看DOCTOR角色的所有权限
SELECT 
    p.permission_code,
    p.permission_name,
    p.description
FROM `role_permission` rp
JOIN `permission` p ON rp.permission_code = p.permission_code
WHERE rp.role_code = 'DOCTOR'
ORDER BY p.permission_code;

-- ================================
-- 10. 测试数据 (可选)
-- ================================

-- 创建测试用户并分配角色
-- 注意: 实际使用时需要替换为真实的account_id

-- 示例: 为用户ID=1分配DOCTOR角色
-- INSERT INTO `account_role` (`account_id`, `role_code`) VALUES
-- (1, 'DOCTOR')
-- ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- 示例: 为用户ID=2分配PATIENT角色
-- INSERT INTO `account_role` (`account_id`, `role_code`) VALUES
-- (2, 'PATIENT')
-- ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);

-- 示例: 为用户ID=999分配ADMIN角色
-- INSERT INTO `account_role` (`account_id`, `role_code`) VALUES
-- (999, 'ADMIN')
-- ON DUPLICATE KEY UPDATE `role_code` = VALUES(`role_code`);
