package com.zixin.utils.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息上下文对象
 * 
 * 存储当前请求的用户完整信息,由Gateway从JWT Token中提取并注入
 * 
 * @author zixin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色列表 (逗号分隔)
     * 例如: "PATIENT,ADMIN"
     */
    private String roles;
    
    /**
     * 用户权限列表 (逗号分隔)
     * 例如: "health:report:read,health:report:write"
     */
    private String authorities;
    
    /**
     * 链路追踪ID
     */
    private String traceId;
    
    /**
     * 用户类型
     * 1-患者, 2-医生, 3-管理员
     */
    private Integer userType;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 主治医生ID (仅患者有)
     */
    private Long attendingDoctorId;
    
    /**
     * 科室ID (仅医生有)
     */
    private Long departmentId;
    
    /**
     * 请求IP
     */
    private String requestIp;
    
    /**
     * 请求时间戳
     */
    private Long requestTime;
}
