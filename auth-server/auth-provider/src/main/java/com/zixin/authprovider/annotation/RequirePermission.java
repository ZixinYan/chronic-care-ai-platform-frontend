package com.zixin.authprovider.annotation;

import java.lang.annotation.*;

/**
 * 需要特定权限才能访问的注解
 * 用于方法级别的权限控制
 * 
 * 使用示例:
 * @RequirePermission({"user:read", "user:write"})
 * public void someMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限列表
     * 用户必须拥有至少一个指定的权限才能访问
     */
    String[] value();
    
    /**
     * 是否需要拥有所有指定的权限
     * true: 需要拥有所有权限(AND)
     * false: 只需要拥有其中一个权限(OR)
     * 默认为false
     */
    boolean requireAll() default false;
}
