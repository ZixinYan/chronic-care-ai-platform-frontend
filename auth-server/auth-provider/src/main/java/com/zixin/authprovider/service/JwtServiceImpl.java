package com.zixin.authprovider.service;

import com.zixin.authapi.api.JwtAPI;
import com.zixin.authprovider.utils.JwtUtils;
import com.zixin.utils.exception.ToBCodeEnum;
import dto.GenTokenRequest;
import dto.GenTokenResponse;
import dto.RefreshTokenRequest;
import dto.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

/**
 * JWT服务实现类
 * 实现双Token认证机制(Access Token + Refresh Token)
 */
@DubboService
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtAPI {

    private final JwtUtils jwtUtils;

    @Value("${jwt.access-token.expiration:1800}")
    private long accessTokenExpiration;

    /**
     * 生成Token对
     * 包含Access Token和Refresh Token
     *
     * @param request 包含用户ID和角色信息的请求
     * @return 包含Access Token和Refresh Token的响应
     */
    @Override
    public GenTokenResponse genToken(GenTokenRequest request) {
        GenTokenResponse response = new GenTokenResponse();

        // 1. 验证请求参数
        if (request.getUserId() == null) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("userId is required");
            log.error("Invalid request: userId is null");
            return response;
        }

        try {
            // 2. 准备角色信息和权限信息
            List<String> roles = request.getRoles();
            if (roles == null) {
                roles = new ArrayList<>();
            }
            
            Set<String> permissions = request.getPermissions();
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            
            log.info("Generating token for user: {}, roles: {}, permissions count: {}", 
                    request.getUserId(), roles, permissions.size());

            // 3. 生成Token对(Access Token + Refresh Token)
            Map<String, String> tokens = jwtUtils.generateTokenPair(
                    request.getUserId(), 
                    request.getUsername(),
                    roles, 
                    permissions);
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            // 4. 验证生成结果
            if (accessToken == null || accessToken.isEmpty() || 
                refreshToken == null || refreshToken.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Token generation failed");
                log.error("Token generation failed for userId: {}", request.getUserId());
                return response;
            }

            // 5. 构造响应
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("Token generated successfully");
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(accessTokenExpiration);
            response.setTokenType("Bearer");

            log.info("Token pair generated successfully for userId: {}, roles: {}", 
                    request.getUserId(), roles);

        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token generation failed: " + e.getMessage());
            log.error("Token generation exception for userId: {}", request.getUserId(), e);
        }

        return response;
    }

    /**
     * 刷新Token
     * 使用Refresh Token生成新的Token对
     *
     * @param request 包含Refresh Token的请求
     * @return 包含新的Access Token和Refresh Token的响应
     */
    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenResponse response = new RefreshTokenResponse();

        // 1. 验证请求参数
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("refreshToken is required");
            log.error("Invalid request: refreshToken is null or empty");
            return response;
        }

        try {
            // 2. 验证Refresh Token并获取用户ID
            Long userId = jwtUtils.validateRefreshToken(request.getRefreshToken());
            if (userId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Invalid or expired refresh token");
                log.warn("Invalid refresh token provided");
                return response;
            }

            // 3. 从旧的Refresh Token中提取角色信息和用户名
            List<String> roles = jwtUtils.getRolesFromToken(request.getRefreshToken());
            String username = jwtUtils.getUsernameFromToken(request.getRefreshToken());
            
            // 4. 重新从数据库加载最新的权限信息
            Set<String> permissions = jwtUtils.loadAndCacheUserAuthorities(userId);

            // 5. 生成新的Token对
            Map<String, String> tokens = jwtUtils.generateTokenPair(userId, username, roles, permissions);
            String newAccessToken = tokens.get("accessToken");
            String newRefreshToken = tokens.get("refreshToken");

            // 6. 验证生成结果
            if (newAccessToken == null || newAccessToken.isEmpty() || 
                newRefreshToken == null || newRefreshToken.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("Token refresh failed");
                log.error("Token refresh failed for userId: {}", userId);
                return response;
            }

            // 7. 构造响应
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("Token refreshed successfully");
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(accessTokenExpiration);
            response.setTokenType("Bearer");

            log.info("Token pair refreshed successfully for userId: {}", userId);

        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("Token refresh failed: " + e.getMessage());
            log.error("Token refresh exception", e);
        }

        return response;
    }
}
