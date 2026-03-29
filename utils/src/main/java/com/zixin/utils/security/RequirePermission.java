package com.zixin.utils.security;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于接口方法上，声明访问该接口需要的权限
 * 
 * 使用示例:
 * @RequirePermission("user:read")
 * @RequirePermission(value = {"user:read", "user:write"}, logical = Logical.AND)
 * 
 * 注意: 使用此注解需要配合AOP切面实现权限校验逻辑
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限列表
     * 权限格式建议: resource:operation
     * 例如: user:read, user:write, order:delete
     */
    String[] value();
    
    /**
     * 多个权限之间的逻辑关系
     * AND: 需要拥有所有权限
     * OR: 只需要拥有其中一个权限
     */
    Logical logical() default Logical.OR;
    
    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 与逻辑 - 需要满足所有权限
         */
        AND,
        
        /**
         * 或逻辑 - 只需满足其中一个权限
         */
        OR
    }
}
