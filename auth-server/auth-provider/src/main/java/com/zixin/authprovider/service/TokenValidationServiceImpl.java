package com.zixin.authprovider.service;

import com.zixin.authapi.api.TokenValidationAPI;
import com.zixin.authprovider.utils.JwtUtils;
import com.zixin.utils.exception.ToBCodeEnum;
import dto.ValidateTokenRequest;
import dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Token验证服务实现类
 * 提供Token验证和撤销功能
 */
@DubboService
@Slf4j
@RequiredArgsConstructor
public class TokenValidationServiceImpl implements TokenValidationAPI {

    private final JwtUtils jwtUtils;

    /**
     * 验证Token是否有效
     * 并返回Token中的用户信息和权限
     *
     * @param request 包含Token的请求
     * @return 验证结果，包含用户信息和权限
     */
    @Override
    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        ValidateTokenResponse response = new ValidateTokenResponse();

        // 1. 验证请求参数
        if (!StringUtils.hasText(request.getToken())) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token is required");
            response.setValid(false);
            log.error("Invalid request: token is null or empty");
            return response;
        }

        try {
            // 2. 验证并解析Token
            Jwt jwt = jwtUtils.validateAndParseToken(request.getToken());
            if (jwt == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Invalid or expired token");
                response.setValid(false);
                log.warn("Token validation failed");
                return response;
            }

            // 3. 检查Token类型
            String tokenType = jwt.getClaim("type");
            if (!"access".equals(tokenType)) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Invalid token type: " + tokenType);
                response.setValid(false);
                log.warn("Invalid token type: {}", tokenType);
                return response;
            }

            // 4. 提取用户信息
            Long userId = Long.parseLong(jwt.getSubject());
            List<String> roles = jwt.getClaim("roles");
            List<String> authorities = jwt.getClaim("authorities");

            // 5. 构造响应
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("Token is valid");
            response.setValid(true);
            response.setUserId(userId);
            response.setRoles(roles);
            response.setAuthorities(authorities);

            log.debug("Token validated successfully for userId: {}", userId);

        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token validation failed: " + e.getMessage());
            response.setValid(false);
            log.error("Token validation exception", e);
        }

        return response;
    }

    /**
     * 撤销Token(登出)
     * 将Token加入黑名单，使其失效
     *
     * @param request 包含Token的请求
     * @return 操作结果
     */
    @Override
    public ValidateTokenResponse revokeToken(ValidateTokenRequest request) {
        ValidateTokenResponse response = new ValidateTokenResponse();

        // 1. 验证请求参数
        if (!StringUtils.hasText(request.getToken())) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token is required");
            log.error("Invalid request: token is null or empty");
            return response;
        }

        try {
            // 2. 解析Token以获取tokenId和过期时间
            Jwt jwt = jwtUtils.validateAndParseToken(request.getToken());
            if (jwt == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Invalid or expired token");
                log.warn("Cannot revoke invalid token");
                return response;
            }

            // 3. 将Token加入黑名单
            String tokenId = jwt.getId();
            jwtUtils.blacklistToken(tokenId, jwt.getExpiresAt());

            // 4. 如果是Refresh Token，也需要从Redis中删除
            String tokenType = jwt.getClaim("type");
            if ("refresh".equals(tokenType)) {
                Long userId = Long.parseLong(jwt.getSubject());
                jwtUtils.revokeRefreshToken(userId, tokenId);
            }

            // 5. 构造响应
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("Token revoked successfully");

            log.info("Token revoked successfully, tokenId: {}, type: {}", tokenId, tokenType);

        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token revocation failed: " + e.getMessage());
            log.error("Token revocation exception", e);
        }

        return response;
    }
}
