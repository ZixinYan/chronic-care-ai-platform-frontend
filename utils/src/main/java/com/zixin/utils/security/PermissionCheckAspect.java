package com.zixin.utils.security;

import com.zixin.utils.context.UserInfoManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限校验切面 - 通用版
 * 拦截带有@RequirePermission和@RequireRole注解的方法
 * 从Gateway注入的请求头中获取用户权限信息进行校验
 * 
 * 使用此切面需要:
 * 1. 在pom.xml中添加spring-boot-starter-aop依赖
 * 3. 在配置类上添加@EnableAspectJAutoProxy
 */
@Slf4j
@Aspect
@Component
public class PermissionCheckAspect {

    /**
     * 校验权限
     */
    @Before("@annotation(com.zixin.utils.security.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("Checking permissions for method: {}.{}",
                method.getDeclaringClass().getSimpleName(), method.getName());
        // 获取注解
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            return;
        }
        
        // 获取当前用户的权限
        String authoritiesStr = UserInfoManager.getAuthorities();
        if (authoritiesStr == null || authoritiesStr.isEmpty()) {
            log.warn("User has no authorities, method: {}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: No authorities");
        }
        log.info("User authorities: {}", authoritiesStr);
        // 将逗号分隔的权限字符串转为集合
        Set<String> userAuthorities = parseToSet(authoritiesStr);
        
        // 获取需要的权限
        String[] requiredPermissions = annotation.value();
        RequirePermission.Logical logical = annotation.logical();
        
        // 校验权限
        boolean hasPermission = checkPermissions(userAuthorities, requiredPermissions, logical);
        
        if (!hasPermission) {
            Long userId = UserInfoManager.getUserId();
            log.warn("User {} does not have required permissions. Required: {}, User has: {}, Logical: {}", 
                    userId, Arrays.toString(requiredPermissions), userAuthorities, logical);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Access denied: Insufficient permissions");
        }
        
        log.debug("Permission check passed for method: {}.{}", 
                method.getDeclaringClass().getSimpleName(), method.getName());
    }

    /**
     * 校验角色
     */
    @Before("@annotation(com.zixin.utils.security.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("Checking roles for method: {}.{}",
                method.getDeclaringClass().getSimpleName(), method.getName());
        // 获取注解
        RequireRole annotation = method.getAnnotation(RequireRole.class);
        if (annotation == null) {
            log.info("No @RequireRole annotation found, skipping role check for method: {}.{}",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }
        
        // 获取当前用户的角色
        String rolesStr = UserInfoManager.getRoles();
        log.info("User roles string: {}", UserInfoManager.getUserContext());
        if (rolesStr == null || rolesStr.isEmpty()) {
            log.warn("User has no roles, method: {}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: No roles");
        }

        // 将逗号分隔的角色字符串转为集合
        Set<String> userRoles = parseToSet(rolesStr);
        log.info("User roles: {}", userRoles);
        // 获取需要的角色
        String[] requiredRoles = annotation.value();
        RequireRole.Logical logical = annotation.logical();

        // 校验角色
        boolean hasRole = checkPermissions(userRoles, requiredRoles, 
                logical == RequireRole.Logical.AND ? RequirePermission.Logical.AND : RequirePermission.Logical.OR);
        
        if (!hasRole) {
            Long userId = UserInfoManager.getUserId();
            log.warn("User {} does not have required roles. Required: {}, User has: {}, Logical: {}", 
                    userId, Arrays.toString(requiredRoles), userRoles, logical);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Access denied: Insufficient roles");
        }
        
        log.info("Role check passed for method: {}.{}",
                method.getDeclaringClass().getSimpleName(), method.getName());
    }

    /**
     * 通用权限校验逻辑
     * 
     * @param userPermissions 用户拥有的权限/角色
     * @param requiredPermissions 需要的权限/角色
     * @param logical 逻辑关系(AND/OR)
     * @return 是否有权限
     */
    private boolean checkPermissions(Set<String> userPermissions, String[] requiredPermissions, 
                                    RequirePermission.Logical logical) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }
        
        if (logical == RequirePermission.Logical.AND) {
            // AND: 必须拥有所有权限
            for (String required : requiredPermissions) {
                if (!userPermissions.contains(required.trim())) {
                    return false;
                }
            }
            return true;
        } else {
            // OR: 只需要拥有其中一个权限
            for (String required : requiredPermissions) {
                if (userPermissions.contains(required.trim())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 将逗号分隔的字符串转为Set集合
     */
    private Set<String> parseToSet(String str) {
        if (str == null || str.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> result = new HashSet<>();
        for (String item : str.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
