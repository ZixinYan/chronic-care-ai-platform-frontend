package dto;

import lombok.Data;

/**
 * 验证Token请求DTO
 */
@Data
public class ValidateTokenRequest {
    /**
     * 待验证的Token
     */
    private String token;
}
