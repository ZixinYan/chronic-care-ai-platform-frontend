package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 权限实体类
 * 
 * 如果需要更细粒度的权限控制(如user:read, medical:record:write),
 * 可以创建此表存储所有可用的权限
 * 
 * 当前系统使用Action枚举(READ/WRITE/ALL)作为权限级别,
 * 通过PermissionExpandUtil展开为具体权限
 *
 */
@Data
@TableName("care_platform_permission")
public class Permission {
    
    /**
     * 权限ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long permissionId;
    
    /**
     * 权限代码 - 用于@RequirePermission注解
     * 格式: resource:operation
     * 
     * 示例:
     * - user:read          读取用户信息
     * - user:write         修改用户信息
     * - user:delete        删除用户
     * - medical:record:read    读取病历
     * - medical:record:write   编辑病历
     * - order:create       创建订单
     * - data:export        数据导出
     */
    private String permissionCode;
    
    /**
     * 权限名称
     * 例如: 读取用户信息, 编辑病历
     */
    private String name;
    
    /**
     * 权限描述
     */
    private String description;
    
    /**
     * 权限分类
     * 例如: USER, MEDICAL, ORDER, SYSTEM
     */
    private String category;
    
    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 版本号
     */
    @Version
    private Integer version;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
