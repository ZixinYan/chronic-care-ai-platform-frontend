package com.zixin.authapi.api;

import dto.ValidateTokenRequest;
import dto.ValidateTokenResponse;

/**
 * Token验证API
 * 用于验证Token的有效性和提取Token信息
 */
public interface TokenValidationAPI {
    
    /**
     * 验证Token是否有效
     * 
     * @param request 包含Token的请求
     * @return 验证结果，包含用户信息和权限
     */
    ValidateTokenResponse validateToken(ValidateTokenRequest request);
    
    /**
     * 撤销Token(登出)
     * 将Token加入黑名单
     * 
     * @param request 包含Token的请求
     * @return 操作结果
     */
    ValidateTokenResponse revokeToken(ValidateTokenRequest request);
}
