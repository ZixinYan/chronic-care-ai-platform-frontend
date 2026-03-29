package dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 生成Token请求DTO
 */
@Data
public class GenTokenRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID(必填)
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表
     * 格式: ["DOCTOR", "PATIENT", "FAMILY"]
     * 使用角色枚举名称,用于@RequireRole注解匹配
     */
    private List<String> roles;
    
    /**
     * 权限列表
     * 格式: ["user:read", "user:write", "medical:record:read"]
     * 使用权限码,用于@RequirePermission注解匹配
     * 
     * 注意: 这些权限应该由AccountService查询并展开后传入,
     * JwtService不再重新查询数据库
     */
    private Set<String> permissions;
}
