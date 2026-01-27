package com.zixin.utils.security;

import lombok.Data;

/**
 * 用户上下文信息
 * 存储当前请求的用户信息，通过ThreadLocal传递
 */
@Data
public class UserContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表(逗号分隔)
     * 例如: "ADMIN,USER"
     */
    private String roles;
    
    /**
     * 权限列表(逗号分隔)
     * 例如: "user:read,user:write,order:read"
     */
    private String authorities;
    
    /**
     * 链路追踪ID
     */
    private String traceId;
}
