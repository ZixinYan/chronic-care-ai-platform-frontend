-- ==========================================
-- 健康报告中心数据库初始化脚本
-- ==========================================

-- 1. 健康报告表
CREATE TABLE IF NOT EXISTS `health_report` (
    `report_id` BIGINT PRIMARY KEY COMMENT '报告ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID (关联patient表)',
    `attending_doctor_id` BIGINT COMMENT '主治医生ID (关联doctor表)',
    `report_type` TINYINT NOT NULL COMMENT '报告类型: 1-图片, 2-文字, 3-PDF',
    `category` VARCHAR(50) COMMENT '报告分类: 体检报告、血液检查、影像检查等',
    `title` VARCHAR(200) NOT NULL COMMENT '报告标题',
    `description` VARCHAR(500) COMMENT '报告描述/备注',
    `file_url` VARCHAR(500) COMMENT '报告文件URL (OSS存储路径)',
    `text_content` TEXT COMMENT '文字内容 (文字类型报告)',
    `report_date` DATE COMMENT '报告日期 (检查/体检日期)',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID (关联account表)',
    `uploader_name` VARCHAR(50) COMMENT '上传者姓名',
    `hospital_name` VARCHAR(100) COMMENT '医疗机构名称',
    `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
    `audit_remark` VARCHAR(200) COMMENT '审核备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `version` INT DEFAULT 1 COMMENT '乐观锁版本号',
    
    INDEX `idx_patient_id` (`patient_id`),
    INDEX `idx_doctor_id` (`attending_doctor_id`),
    INDEX `idx_report_date` (`report_date`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康报告表';

-- 2. 患者表添加主治医生字段 (如果不存在)
-- 注意: 如果patient表已存在,需要手动执行此语句
ALTER TABLE `patient` 
ADD COLUMN `attending_doctor_id` BIGINT COMMENT '主治医生ID (关联doctor表)' AFTER `account_id`;

-- 3. 示例数据 (可选)
-- 插入示例报告数据
-- INSERT INTO `health_report` 
-- (`report_id`, `patient_id`, `attending_doctor_id`, `report_type`, `category`, `title`, 
--  `description`, `report_date`, `uploader_id`, `uploader_name`, `hospital_name`, `status`)
-- VALUES
-- (1, 1001, 2001, 1, '体检报告', '2024年度健康体检报告', '年度常规体检', 
--  '2024-01-15', 1001, '张三', '北京协和医院', 1),
-- (2, 1001, 2001, 2, '血液检查', '血常规检查报告', '常规血液检测', 
--  '2024-02-20', 2001, '李医生', '北京协和医院', 1);

-- 4. 报告类型说明
/*
报告类型 (report_type):
  1 - IMAGE  : 图片报告 (PNG, JPG等)
  2 - TEXT   : 文字报告
  3 - PDF    : PDF文档

审核状态 (status):
  0 - PENDING  : 待审核
  1 - APPROVED : 已通过
  2 - REJECTED : 已拒绝

报告分类 (category) 建议值:
  - 体检报告
  - 血液检查
  - 影像检查 (X光、CT、MRI等)
  - 病理报告
  - 心电图
  - 超声检查
*/
