package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import com.zixin.accountapi.enums.Action;
import com.zixin.accountapi.enums.RoleCode;
import lombok.Data;

import java.util.Date;

/**
 * 角色-权限关联表
 * 
 * 当前设计:
 * - roleCode: 角色代码(对应RoleCode枚举: 1-DOCTOR, 2-PATIENT, 3-FAMILY 4-ADMIN)
 * - actionCode: 权限代码(对应Action枚举: 1-READ, 2-WRITE, 3-ALL)
 * 
 * 权限工作流程:
 * 1. 用户登录时,根据用户的角色(roleCode)查询此表
 * 2. 获取该角色对应的所有actionCode
 * 3. 通过PermissionExpandUtil.expand()将actionCode展开为具体权限
 * 4. 将权限列表放入JWT Token
 * 5. Gateway提取JWT权限信息注入请求头
 * 6. 下游服务通过@RequirePermission注解校验
 * 
 * 注意: 
 * - 当前使用actionCode作为权限级别(READ/WRITE/ALL)
 * - 如需细粒度权限(如user:read, medical:record:write),
 *   建议新增permission_code字段存储具体权限码
 */
@Data
@TableName("care_platform_role_permission")
public class RolePermission {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色代码 - 对应RoleCode枚举
     * 1 - DOCTOR (医生)
     * 2 - PATIENT (患者)
     * 3 - FAMILY (家属)
     */
    private Integer roleCode;

    /**
     * 权限代码 - 对应Action枚举
     * 1 - READ (可读)
     * 2 - WRITE (可编辑)
     * 3 - ALL (拥有全部权限)
     * 
     * 注意: 这是基础权限级别
     * 通过PermissionExpandUtil可以展开为具体的权限列表
     */
    private Integer actionCode;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;
    
    // ==================== 工具方法 ====================
    
    /**
     * 获取角色枚举
     */
    public RoleCode getRoleCodeEnum() {
        return RoleCode.fromCode(this.roleCode);
    }
    
    /**
     * 获取权限枚举
     */
    public Action getActionEnum() {
        return Action.fromCode(this.actionCode);
    }
    
    /**
     * 获取角色名称(用于@RequireRole注解)
     */
    public String getRoleName() {
        return getRoleCodeEnum().name();
    }
}
