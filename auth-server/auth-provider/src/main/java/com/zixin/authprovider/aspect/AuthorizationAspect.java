package com.zixin.authprovider.aspect;

import com.zixin.authprovider.annotation.RequirePermission;
import com.zixin.authprovider.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限控制切面
 * 拦截带有@RequireRole和@RequirePermission注解的方法
 * 验证当前用户是否拥有所需的角色或权限
 */
@Slf4j
@Aspect
@Component
public class AuthorizationAspect {

    /**
     * 拦截@RequireRole注解的方法
     */
    @Around("@annotation(com.zixin.authprovider.annotation.RequireRole) || " +
            "@within(com.zixin.authprovider.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 2. 获取注解
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
        }
        
        if (requireRole == null) {
            return joinPoint.proceed();
        }

        // 3. 获取当前用户的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to method: {}", method.getName());
            throw new AccessDeniedException("Authentication required");
        }

        // 4. 获取用户角色
        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除"ROLE_"前缀
                .collect(Collectors.toSet());

        // 5. 检查角色
        String[] requiredRoles = requireRole.value();
        boolean hasPermission;
        
        if (requireRole.requireAll()) {
            // 需要拥有所有角色
            hasPermission = userRoles.containsAll(Arrays.asList(requiredRoles));
        } else {
            // 只需要拥有其中一个角色
            hasPermission = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }

        if (!hasPermission) {
            log.warn("Access denied for user: {}, required roles: {}, user roles: {}", 
                    authentication.getName(), Arrays.toString(requiredRoles), userRoles);
            throw new AccessDeniedException("Insufficient role privileges");
        }

        log.debug("Role check passed for user: {}, method: {}", 
                authentication.getName(), method.getName());
        return joinPoint.proceed();
    }

    /**
     * 拦截@RequirePermission注解的方法
     */
    @Around("@annotation(com.zixin.authprovider.annotation.RequirePermission) || " +
            "@within(com.zixin.authprovider.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 2. 获取注解
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = joinPoint.getTarget().getClass().getAnnotation(RequirePermission.class);
        }
        
        if (requirePermission == null) {
            return joinPoint.proceed();
        }

        // 3. 获取当前用户的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to method: {}", method.getName());
            throw new AccessDeniedException("Authentication required");
        }

        // 4. 获取用户权限
        Set<String> userPermissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_")) // 排除角色
                .collect(Collectors.toSet());

        // 5. 检查权限
        String[] requiredPermissions = requirePermission.value();
        boolean hasPermission;
        
        if (requirePermission.requireAll()) {
            // 需要拥有所有权限
            hasPermission = userPermissions.containsAll(Arrays.asList(requiredPermissions));
        } else {
            // 只需要拥有其中一个权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("Access denied for user: {}, required permissions: {}, user permissions: {}", 
                    authentication.getName(), Arrays.toString(requiredPermissions), userPermissions);
            throw new AccessDeniedException("Insufficient permissions");
        }

        log.debug("Permission check passed for user: {}, method: {}", 
                authentication.getName(), method.getName());
        return joinPoint.proceed();
    }
}
