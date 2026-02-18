package com.zixin.accountprovider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色配置
 * 
 * 用于配置用户注册时的默认角色
 */
@Data
@Component
@ConfigurationProperties(prefix = "account.role")
public class RoleConfig {
    
    /**
     * 默认角色列表
     * 用户注册时如果没有指定角色，会自动分配这些角色
     * 
     * 配置示例:
     * account:
     *   role:
     *     default-roles:
     *       - 3  # PATIENT角色
     */
    private List<Integer> defaultRoles = new ArrayList<>();
    
    /**
     * 获取默认角色列表
     * 如果没有配置，返回空列表
     */
    public List<Integer> getDefaultRoles() {
        if (defaultRoles == null) {
            defaultRoles = new ArrayList<>();
        }
        return defaultRoles;
    }
}
