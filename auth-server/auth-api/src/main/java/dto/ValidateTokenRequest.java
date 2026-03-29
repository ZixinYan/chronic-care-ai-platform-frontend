package dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证Token请求DTO
 */
@Data
public class ValidateTokenRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 待验证的Token
     */
    private String token;
}
