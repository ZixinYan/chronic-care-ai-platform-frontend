package com.zixin.utils.security;

import java.lang.annotation.*;

/**
 * 角色校验注解
 * 用于接口方法上，声明访问该接口需要的角色
 * 
 * 使用示例:
 * @RequireRole("ADMIN")
 * @RequireRole(value = {"ADMIN", "MANAGER"}, logical = Logical.OR)
 * 
 * 注意: 使用此注解需要配合AOP切面实现角色校验逻辑
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 需要的角色列表
     * 例如: ADMIN, USER, MANAGER, DOCTOR, PATIENT
     */
    String[] value();
    
    /**
     * 多个角色之间的逻辑关系
     * AND: 需要拥有所有角色
     * OR: 只需要拥有其中一个角色
     */
    Logical logical() default Logical.OR;
    
    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 与逻辑 - 需要满足所有角色
         */
        AND,
        
        /**
         * 或逻辑 - 只需满足其中一个角色
         */
        OR
    }
}
