package dto;

import lombok.Data;

import java.util.List;

/**
 * 生成Token请求DTO
 */
@Data
public class GenTokenRequest {
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
     * 格式: ["ROLE_ADMIN", "ROLE_USER"]
     */
    private List<String> roles;
}
