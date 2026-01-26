package com.zixin.authprovider.annotation;

import java.lang.annotation.*;

/**
 * 需要特定角色才能访问的注解
 * 用于方法级别的角色权限控制
 * 
 * 使用示例:
 * @RequireRole({"ADMIN", "MANAGER"})
 * public void someMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 需要的角色列表
     * 用户必须拥有至少一个指定的角色才能访问
     */
    String[] value();
    
    /**
     * 是否需要拥有所有指定的角色
     * true: 需要拥有所有角色(AND)
     * false: 只需要拥有其中一个角色(OR)
     * 默认为false
     */
    boolean requireAll() default false;
}
