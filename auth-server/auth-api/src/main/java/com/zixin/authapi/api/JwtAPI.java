package com.zixin.authapi.api;

import dto.GenTokenRequest;
import dto.GenTokenResponse;
import dto.RefreshTokenRequest;
import dto.RefreshTokenResponse;

/**
 * 正常登录注册流程
 */
public interface JwtAPI {
    GenTokenResponse genToken(GenTokenRequest genTokenRequest);
    /**
     * 刷新Token
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
