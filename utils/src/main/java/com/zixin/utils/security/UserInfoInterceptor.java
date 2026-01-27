package com.zixin.utils.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器 - 基础版
 * 从Gateway注入的请求头中提取用户信息，存储到ThreadLocal
 * 
 * 各个下游服务可以继承此类，也可以直接使用此类
 * 
 * Gateway会在验证Token后，将用户信息注入到以下请求头：
 * - X-Trace-Id: 链路追踪ID
 * - X-User-Id: 用户ID
 * - X-User-Roles: 角色列表(逗号分隔)
 * - X-User-Authorities: 权限列表(逗号分隔)
 */
@Slf4j
@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    // 请求头名称常量
    protected static final String HEADER_TRACE_ID = "X-Trace-Id";
    protected static final String HEADER_USER_ID = "X-User-Id";
    protected static final String HEADER_USER_ROLES = "X-User-Roles";
    protected static final String HEADER_USER_AUTHORITIES = "X-User-Authorities";
    protected static final String HEADER_USERNAME = "X-Username";

    /**
     * 线程本地变量，用于存储当前请求的用户信息
     */
    private static final ThreadLocal<UserContext> USER_CONTEXT_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头提取用户信息
            String traceId = request.getHeader(HEADER_TRACE_ID);
            String userIdStr = request.getHeader(HEADER_USER_ID);
            String username = request.getHeader(HEADER_USERNAME);
            String roles = request.getHeader(HEADER_USER_ROLES);
            String authorities = request.getHeader(HEADER_USER_AUTHORITIES);

            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    
                    // 创建用户上下文
                    UserContext context = new UserContext();
                    context.setUserId(userId);
                    context.setUsername(username);
                    context.setRoles(roles);
                    context.setAuthorities(authorities);
                    context.setTraceId(traceId);
                    
                    // 设置到ThreadLocal
                    USER_CONTEXT_HOLDER.set(context);
                    
                    log.debug("User context set - userId: {}, username: {}, roles: {}, authorities: {}, traceId: {}", 
                            userId, username, roles, authorities, traceId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid user ID format: {}", userIdStr);
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract user info from headers", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 请求完成后清除ThreadLocal，避免内存泄漏
        USER_CONTEXT_HOLDER.remove();
    }

    // ==================== 静态工具方法 ====================

    /**
     * 获取完整的用户上下文
     */
    public static UserContext getUserContext() {
        return USER_CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前请求的用户ID
     */
    public static Long getCurrentUserId() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前请求的用户名
     */
    public static String getCurrentUsername() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前请求的用户角色
     */
    public static String getCurrentUserRoles() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getRoles() : null;
    }

    /**
     * 获取当前请求的用户权限
     */
    public static String getCurrentUserAuthorities() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getAuthorities() : null;
    }

    /**
     * 获取当前请求的追踪ID
     */
    public static String getCurrentTraceId() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getTraceId() : null;
    }

    /**
     * 手动设置用户上下文(用于测试或特殊场景)
     */
    public static void setUserContext(UserContext context) {
        USER_CONTEXT_HOLDER.set(context);
    }

    /**
     * 手动清除用户上下文
     */
    public static void clearUserContext() {
        USER_CONTEXT_HOLDER.remove();
    }
}
