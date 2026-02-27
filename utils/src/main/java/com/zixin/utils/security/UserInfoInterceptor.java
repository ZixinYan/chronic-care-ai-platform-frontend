package com.zixin.utils.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.zixin.utils.constant.HeaderConstant.*;

/**
 * 用户信息拦截器 (已废弃)
 * 
 * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager} 和 {@link com.zixin.utils.interceptor.UserInfoExtractInterceptor}
 * 
 * 迁移指南:
 * 旧代码: UserInfoInterceptor.getCurrentUserId()
 * 新代码: UserInfoManager.getUserId()
 * 
 * 旧代码: UserInfoInterceptor.getCurrentUsername()
 * 新代码: UserInfoManager.getUsername()
 * 
 * 该类保留仅为了向后兼容，建议尽快迁移到新的UserInfoManager
 */
@Slf4j
@Component
@Deprecated
public class UserInfoInterceptor implements HandlerInterceptor {
    

    /**
     * 线程本地变量，用于存储当前请求的用户信息
     * @deprecated 请使用 UserInfoManager
     */
    @Deprecated
    private static final ThreadLocal<UserContext> USER_CONTEXT_HOLDER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头提取用户信息
            String traceId = request.getHeader(TRACE_ID);
            String userIdStr = request.getHeader(USER_ID);
            String username = request.getHeader(USERNAME);
            String roles = request.getHeader(USER_ROLES);
            String authorities = request.getHeader(USER_AUTHORITIES);

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

    // ==================== 静态工具方法 (已废弃) ====================

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getUserContext()}
     */
    @Deprecated
    public static UserContext getUserContext() {
        return USER_CONTEXT_HOLDER.get();
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getUserId()}
     */
    @Deprecated
    public static Long getCurrentUserId() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getUserId() : null;
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getUsername()}
     */
    @Deprecated
    public static String getCurrentUsername() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getUsername() : null;
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getRoles()}
     */
    @Deprecated
    public static String getCurrentUserRoles() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getRoles() : null;
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getAuthorities()}
     */
    @Deprecated
    public static String getCurrentUserAuthorities() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getAuthorities() : null;
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#getTraceId()}
     */
    @Deprecated
    public static String getCurrentTraceId() {
        UserContext context = USER_CONTEXT_HOLDER.get();
        return context != null ? context.getTraceId() : null;
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#setUserContext(com.zixin.utils.context.UserInfoContext)}
     */
    @Deprecated
    public static void setUserContext(UserContext context) {
        USER_CONTEXT_HOLDER.set(context);
    }

    /**
     * @deprecated 请使用 {@link com.zixin.utils.context.UserInfoManager#clearUserContext()}
     */
    @Deprecated
    public static void clearUserContext() {
        USER_CONTEXT_HOLDER.remove();
    }
}
