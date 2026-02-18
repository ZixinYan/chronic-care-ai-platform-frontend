package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import com.zixin.accountapi.enums.RoleCode;
import lombok.Data;

import java.util.Date;

/**
 * 角色实体类
 * 
 * 角色设计说明:
 * 1. code字段: 存储RoleCode枚举的code值(1-DOCTOR, 2-PATIENT, 3-FAMILY 4-ADMIN)
 * 2. 用于数据库存储和业务逻辑
 * 3. 在生成JWT Token时，需要将code转换为枚举名称(如"DOCTOR")
 * 4. @RequireRole注解使用枚举名称进行匹配
 * 
 * 权限关联:
 * - 通过RolePermission表关联具体权限
 * - 一个角色可以拥有多个权限
 * - 用户可以拥有多个角色
 */
@Data
@TableName("care_platform_role")
public class Role {

    /**
     * 角色ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;

    /**
     * 角色代码 - 对应RoleCode枚举
     * 1 - DOCTOR (医生)
     * 2 - PATIENT (患者)
     * 3 - FAMILY (家属)
     * 
     * 注意: 
     * - 数据库存储枚举的code值
     * - 生成JWT时转换为枚举名称(用于@RequireRole注解)
     */
    private Integer code;
    
    /**
     * 角色名称
     * 例如: 医生, 患者, 家属
     */
    private String name;
    
    /**
     * 角色描述
     */
    private String description;

    /**
     * 业务可配置权限 - 对应Action枚举
     * 1 - READ (可读)
     * 2 - WRITE (可编辑)
     * 3 - ALL (拥有全部权限)
     * 
     * 这是一个基础权限级别，具体的细粒度权限由RolePermission表管理
     */
    private Integer action;

    /**
     * 删除标记 (0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取角色枚举
     */
    public RoleCode getRoleCode() {
        return RoleCode.fromCode(this.code);
    }
    
    /**
     * 获取角色名称(用于@RequireRole注解)
     * 返回枚举名称: DOCTOR, PATIENT, FAMILY
     */
    public String getRoleCodeName() {
        return getRoleCode().name();
    }
}
