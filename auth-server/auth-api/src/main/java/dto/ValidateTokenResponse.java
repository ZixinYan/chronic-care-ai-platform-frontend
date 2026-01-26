package dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 验证Token响应DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ValidateTokenResponse extends BaseResponse {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色列表
     */
    private List<String> roles;
    
    /**
     * 权限列表
     */
    private List<String> authorities;
    
    /**
     * Token是否有效
     */
    private Boolean valid;
}
