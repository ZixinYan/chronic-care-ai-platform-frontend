package dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 刷新Token响应DTO
 * 返回新的Access Token和Refresh Token
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RefreshTokenResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 新的访问令牌(Access Token)
     */
    private String accessToken;
    
    /**
     * 新的刷新令牌(Refresh Token)
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
