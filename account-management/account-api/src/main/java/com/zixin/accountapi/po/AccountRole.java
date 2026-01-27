package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import com.zixin.accountapi.enums.RoleCode;
import lombok.Data;
import java.util.Date;

/**
 * 用户-角色关联表 (AccountRole)
 * 
 * 用于实现用户和角色的多对多关系:
 * - 一个用户可以拥有多个角色 (例如: 既是医生又是管理员)
 * - 一个角色可以被多个用户拥有 (例如: 多个用户都是医生)
 *
 * 
 * 数据流转:
 * 1. 用户登录 → 查询此表获取用户的所有角色
 * 2. 根据角色 → 查询RolePermission表获取权限
 * 3. 生成JWT Token → 包含roles和authorities
 * 4. 下游服务 → 使用@RequireRole和@RequirePermission注解鉴权
 */
@Data
@TableName("care_platform_user_role")
public class AccountRole {
    
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long accountRoleId;
    
    /**
     * 用户ID - 关联Account表
     */
    private Long userId;
    
    /**
     * 角色代码 - 对应RoleCode枚举
     * 1 - DOCTOR (医生)
     * 2 - PATIENT (患者)
     * 3 - FAMILY (家属)
     * 
     * 注意: 存储的是枚举的code值,在生成JWT时需要转换为枚举名称
     */
    private Integer roleCode;
    
    /**
     * 授权时间
     */
    private Date createTime;
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取角色枚举
     */
    public RoleCode getRoleCodeEnum() {
        return RoleCode.fromCode(this.roleCode);
    }
    
    /**
     * 获取角色名称(用于@RequireRole注解和JWT Token)
     * 返回枚举名称: "DOCTOR", "PATIENT", "FAMILY"
     */
    public String getRoleName() {
        return getRoleCodeEnum().name();
    }
}
