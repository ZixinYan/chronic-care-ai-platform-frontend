package dto;


import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生成Token响应DTO
 * 包含Access Token和Refresh Token
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenTokenResponse extends BaseResponse {
    /**
     * 访问令牌(Access Token)
     * 用于API访问，短期有效(默认30分钟)
     */
    private String accessToken;
    
    /**
     * 刷新令牌(Refresh Token)
     * 用于刷新访问令牌，长期有效(默认7天)
     */
    private String refreshToken;
    
    /**
     * 访问令牌过期时间(秒)
     */
    private Long expiresIn;
    
    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";
}
