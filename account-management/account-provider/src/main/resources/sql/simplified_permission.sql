-- =============================================
-- 简化权限体系 - 数据库初始化脚本
-- Author: zixin
-- Create Date: 2024
-- =============================================

-- 1. 权限表 (简化版)
-- 只保留读写权限,按模块划分
INSERT INTO `permission` (`permission_id`, `permission_code`, `permission_name`, `description`, `create_time`) VALUES
-- 医生模块权限
(1, 'doctor:read', '医生读权限', '查看医生信息、日程列表、日程详情', NOW()),
(2, 'doctor:write', '医生写权限', '生成日程、完成日程、取消日程、更新状态', NOW()),

-- 患者模块权限
(3, 'patient:read', '患者读权限', '查看患者信息、健康数据', NOW()),
(4, 'patient:write', '患者写权限', '修改患者信息、上传健康数据', NOW()),

-- 管理员权限
(5, 'admin:all', '管理员全部权限', '系统管理员拥有所有权限', NOW());

-- 2. 角色权限关联表
-- ADMIN (1) - 拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(1, 1, NOW()),
(1, 2, NOW()),
(1, 3, NOW()),
(1, 4, NOW()),
(1, 5, NOW());

-- DOCTOR (2) - 拥有医生读写权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(2, 1, NOW()),
(2, 2, NOW());

-- PATIENT (3) - 拥有患者读写权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(3, 3, NOW()),
(3, 4, NOW());

-- 3. 权限说明
/*
权限设计原则:
1. 简单明了: 只区分读(read)和写(write)权限
2. 模块化: 按业务模块划分权限(doctor, patient, admin等)
3. 命名规范: {module}:{operation} 格式,如 doctor:read, patient:write

权限码对应关系:
- doctor:read  -> 查看医生信息、日程等所有查询操作
- doctor:write -> 创建、修改、删除医生相关数据的所有写操作
- patient:read -> 查看患者信息、健康数据等所有查询操作
- patient:write -> 修改患者信息、上传健康数据等所有写操作
- admin:all -> 管理员拥有所有权限

角色默认权限:
- ADMIN: 所有权限
- DOCTOR: doctor:read, doctor:write
- PATIENT: patient:read, patient:write

扩展方式:
新增模块只需添加两个权限:
- {module}:read
- {module}:write
*/
