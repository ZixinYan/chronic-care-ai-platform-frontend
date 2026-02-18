package com.zixin.utils.interceptor;

import com.zixin.utils.context.UserInfoContext;
import com.zixin.utils.context.UserInfoManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * 用户信息拦截器
 * 
 * 从Gateway注入的请求头中提取用户完整信息,存储到ThreadLocal (UserInfoManager)
 * 
 * @author zixin
 */
@Slf4j
@Component
public class UserInfoExtractInterceptor implements HandlerInterceptor {
    
    private static final String TRACE_ID = "X-Trace-Id";
    private static final String USER_ID = "X-User-Id";
    private static final String USERNAME = "X-Username";
    private static final String USER_TYPE = "X-User-Type";
    private static final String USER_ROLES = "X-User-Roles";
    private static final String USER_AUTHORITIES = "X-User-Authorities";
    private static final String REAL_NAME = "X-Real-Name";
    private static final String NICKNAME = "X-Nickname";
    private static final String PHONE = "X-Phone";
    private static final String EMAIL = "X-Email";
    private static final String ATTENDING_DOCTOR_ID = "X-Attending-Doctor-Id";
    private static final String DEPARTMENT_ID = "X-Department-Id";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头提取用户信息
            String traceId = request.getHeader(TRACE_ID);
            String userIdStr = request.getHeader(USER_ID);
            String username = request.getHeader(USERNAME);
            String userTypeStr = request.getHeader(USER_TYPE);
            String roles = request.getHeader(USER_ROLES);
            String authorities = request.getHeader(USER_AUTHORITIES);
            String realName = request.getHeader(REAL_NAME);
            String nickname = request.getHeader(NICKNAME);
            String phone = request.getHeader(PHONE);
            String email = request.getHeader(EMAIL);
            String attendingDoctorIdStr = request.getHeader(ATTENDING_DOCTOR_ID);
            String departmentIdStr = request.getHeader(DEPARTMENT_ID);
            
            // 构建用户上下文
            UserInfoContext context = UserInfoContext.builder()
                    .traceId(traceId)
                    .userId(userIdStr != null ? Long.parseLong(userIdStr) : null)
                    .username(username)
                    .userType(userTypeStr != null ? Integer.parseInt(userTypeStr) : null)
                    .roles(roles)
                    .authorities(authorities)
                    .realName(realName)
                    .nickname(nickname)
                    .phone(phone)
                    .email(email)
                    .attendingDoctorId(attendingDoctorIdStr != null ? Long.parseLong(attendingDoctorIdStr) : null)
                    .departmentId(departmentIdStr != null ? Long.parseLong(departmentIdStr) : null)
                    .requestIp(getClientIp(request))
                    .requestTime(System.currentTimeMillis())
                    .build();
            
            // 存储到ThreadLocal
            UserInfoManager.setUserContext(context);
            
            log.debug("UserInfoExtractInterceptor - 用户信息已注入 - userId: {}, username: {}, traceId: {}", 
                    context.getUserId(), context.getUsername(), context.getTraceId());
            
        } catch (Exception e) {
            log.error("UserInfoExtractInterceptor - 提取用户信息失败", e);
            // 异常不影响请求继续执行
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 清理ThreadLocal，防止内存泄漏
        UserInfoManager.clearUserContext();
        log.debug("UserInfoExtractInterceptor - 用户信息已清理");
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
