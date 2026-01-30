package com.zixin.utils.context;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户信息管理器
 * 
 * 使用ThreadLocal存储当前请求的用户完整信息
 * 由Gateway/Interceptor在请求开始时注入,请求结束时清理
 * 
 * 使用示例:
 * <pre>
 * // 获取当前用户ID
 * Long userId = UserInfoManager.getUserId();
 * 
 * // 获取当前用户完整信息
 * UserInfoContext context = UserInfoManager.getUserContext();
 * 
 * // 获取主治医生ID
 * Long doctorId = UserInfoManager.getAttendingDoctorId();
 * </pre>
 * 
 * @author zixin
 */
@Slf4j
public class UserInfoManager {
    
    /**
     * ThreadLocal存储用户上下文
     */
    private static final ThreadLocal<UserInfoContext> USER_CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置用户上下文
     * 
     * @param context 用户上下文
     */
    public static void setUserContext(UserInfoContext context) {
        USER_CONTEXT_HOLDER.set(context);
        log.debug("setUserContext - userId: {}, username: {}, traceId: {}", 
                context != null ? context.getUserId() : null,
                context != null ? context.getUsername() : null,
                context != null ? context.getTraceId() : null);
    }
    
    /**
     * 获取用户上下文
     * 
     * @return 用户上下文,未设置返回null
     */
    public static UserInfoContext getUserContext() {
        return USER_CONTEXT_HOLDER.get();
    }
    
    /**
     * 清理用户上下文
     * 
     * 必须在请求结束时调用,防止ThreadLocal内存泄漏
     */
    public static void clearUserContext() {
        UserInfoContext context = USER_CONTEXT_HOLDER.get();
        if (context != null) {
            log.debug("clearUserContext - userId: {}, traceId: {}", 
                    context.getUserId(), context.getTraceId());
        }
        USER_CONTEXT_HOLDER.remove();
    }
    
    // ==================== 便捷获取方法 ====================
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID,未登录返回null
     */
    public static Long getUserId() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名,未登录返回null
     */
    public static String getUsername() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getUsername() : null;
    }
    
    /**
     * 获取当前用户角色
     * 
     * @return 角色列表(逗号分隔),未登录返回null
     */
    public static String getRoles() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getRoles() : null;
    }
    
    /**
     * 获取当前用户权限
     * 
     * @return 权限列表(逗号分隔),未登录返回null
     */
    public static String getAuthorities() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getAuthorities() : null;
    }
    
    /**
     * 获取链路追踪ID
     * 
     * @return 追踪ID
     */
    public static String getTraceId() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getTraceId() : null;
    }
    
    /**
     * 获取用户类型
     * 
     * @return 用户类型 (1-患者, 2-医生, 3-管理员)
     */
    public static Integer getUserType() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getUserType() : null;
    }
    
    /**
     * 获取真实姓名
     * 
     * @return 真实姓名
     */
    public static String getRealName() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getRealName() : null;
    }
    
    /**
     * 获取昵称
     * 
     * @return 昵称
     */
    public static String getNickname() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getNickname() : null;
    }
    
    /**
     * 获取手机号
     * 
     * @return 手机号
     */
    public static String getPhone() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getPhone() : null;
    }
    
    /**
     * 获取邮箱
     * 
     * @return 邮箱
     */
    public static String getEmail() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getEmail() : null;
    }
    
    /**
     * 获取主治医生ID (仅患者有)
     * 
     * @return 主治医生ID
     */
    public static Long getAttendingDoctorId() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getAttendingDoctorId() : null;
    }
    
    /**
     * 获取科室ID (仅医生有)
     * 
     * @return 科室ID
     */
    public static Long getDepartmentId() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getDepartmentId() : null;
    }
    
    /**
     * 获取请求IP
     * 
     * @return 请求IP
     */
    public static String getRequestIp() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getRequestIp() : null;
    }
    
    /**
     * 获取请求时间戳
     * 
     * @return 请求时间戳
     */
    public static Long getRequestTime() {
        UserInfoContext context = getUserContext();
        return context != null ? context.getRequestTime() : null;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 检查当前用户是否已登录
     * 
     * @return true-已登录, false-未登录
     */
    public static boolean isUserLoggedIn() {
        Long userId = getUserId();
        return userId != null;
    }
    
    /**
     * 获取当前用户ID,未登录抛出异常
     * 
     * @return 用户ID
     * @throws IllegalStateException 当前用户未登录
     */
    public static Long getUserIdOrThrow() {
        Long userId = getUserId();
        if (userId == null) {
            log.warn("getUserIdOrThrow - 用户未登录");
            throw new IllegalStateException("用户未登录,请先登录");
        }
        return userId;
    }
    
    /**
     * 检查当前用户是否有指定角色
     * 
     * @param role 角色名称
     * @return true-有该角色, false-没有该角色
     */
    public static boolean hasRole(String role) {
        String roles = getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(role);
    }
    
    /**
     * 检查当前用户是否有指定权限
     * 
     * @param permission 权限代码
     * @return true-有该权限, false-没有该权限
     */
    public static boolean hasPermission(String permission) {
        String authorities = getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.contains(permission);
    }
    
    /**
     * 检查当前用户是否是患者
     * 
     * @return true-是患者, false-不是患者
     */
    public static boolean isPatient() {
        Integer userType = getUserType();
        return userType != null && userType == 1;
    }
    
    /**
     * 检查当前用户是否是医生
     * 
     * @return true-是医生, false-不是医生
     */
    public static boolean isDoctor() {
        Integer userType = getUserType();
        return userType != null && userType == 2;
    }
    
    /**
     * 检查当前用户是否是管理员
     * 
     * @return true-是管理员, false-不是管理员
     */
    public static boolean isAdmin() {
        Integer userType = getUserType();
        return userType != null && userType == 3;
    }
}
