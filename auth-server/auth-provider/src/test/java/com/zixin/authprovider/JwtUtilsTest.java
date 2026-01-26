package com.zixin.authprovider.test;

import com.zixin.authprovider.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
@Slf4j
@SpringBootTest
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 测试生成Access Token
     */
    @Test
    public void testGenerateAccessToken() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("ADMIN", "USER");

        String accessToken = jwtUtils.generateAccessToken(userId, roles);

        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
        log.info("Generated access token: {}", accessToken);

        // 验证Token
        Jwt jwt = jwtUtils.validateAndParseToken(accessToken);
        assertNotNull(jwt);
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals("access", jwt.getClaim("type"));
    }

    /**
     * 测试生成Refresh Token
     */
    @Test
    public void testGenerateRefreshToken() {
        Long userId = 1001L;

        String refreshToken = jwtUtils.generateRefreshToken(userId);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        log.info("Generated refresh token: {}", refreshToken);

        // 验证Refresh Token
        Long validatedUserId = jwtUtils.validateRefreshToken(refreshToken);
        assertNotNull(validatedUserId);
        assertEquals(userId, validatedUserId);
    }

    /**
     * 测试生成Token对
     */
    @Test
    public void testGenerateTokenPair() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("ADMIN", "USER");

        Map<String, String> tokens = jwtUtils.generateTokenPair(userId, roles);

        assertNotNull(tokens);
        assertTrue(tokens.containsKey("accessToken"));
        assertTrue(tokens.containsKey("refreshToken"));

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        log.info("Generated token pair - Access: {}, Refresh: {}", accessToken, refreshToken);
    }

    /**
     * 测试Token验证
     */
    @Test
    public void testValidateToken() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("ADMIN", "USER");

        String accessToken = jwtUtils.generateAccessToken(userId, roles);
        Jwt jwt = jwtUtils.validateAndParseToken(accessToken);

        assertNotNull(jwt);
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals("access", jwt.getClaim("type"));
        
        List<String> tokenRoles = jwt.getClaim("roles");
        assertNotNull(tokenRoles);
        assertTrue(tokenRoles.containsAll(roles));

        log.info("Token validated successfully");
    }

    /**
     * 测试Token黑名单
     */
    @Test
    public void testTokenBlacklist() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("USER");

        String accessToken = jwtUtils.generateAccessToken(userId, roles);
        Jwt jwt = jwtUtils.validateAndParseToken(accessToken);
        assertNotNull(jwt);

        // 加入黑名单
        jwtUtils.blacklistToken(jwt.getId(), jwt.getExpiresAt());

        // 再次验证应该失败
        Jwt blacklistedJwt = jwtUtils.validateAndParseToken(accessToken);
        assertNull(blacklistedJwt);

        log.info("Token blacklist test passed");
    }

    /**
     * 测试撤销Refresh Token
     */
    @Test
    public void testRevokeRefreshToken() {
        Long userId = 1001L;

        String refreshToken = jwtUtils.generateRefreshToken(userId);
        
        // 第一次验证应该成功
        Long validatedUserId = jwtUtils.validateRefreshToken(refreshToken);
        assertNotNull(validatedUserId);
        assertEquals(userId, validatedUserId);

        // 提取tokenId并撤销
        Jwt jwt = jwtUtils.validateAndParseToken(refreshToken);
        String tokenId = jwt.getId();
        jwtUtils.revokeRefreshToken(userId, tokenId);

        // 再次验证应该失败
        Long revokedValidation = jwtUtils.validateRefreshToken(refreshToken);
        assertNull(revokedValidation);

        log.info("Refresh token revocation test passed");
    }

    /**
     * 测试从Token提取用户ID
     */
    @Test
    public void testGetUserIdFromToken() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("USER");

        String accessToken = jwtUtils.generateAccessToken(userId, roles);
        Long extractedUserId = jwtUtils.getUserIdFromToken(accessToken);

        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);

        log.info("Extracted userId: {}", extractedUserId);
    }

    /**
     * 测试从Token提取角色
     */
    @Test
    public void testGetRolesFromToken() {
        Long userId = 1001L;
        List<String> roles = Arrays.asList("ADMIN", "USER", "MANAGER");

        String accessToken = jwtUtils.generateAccessToken(userId, roles);
        List<String> extractedRoles = jwtUtils.getRolesFromToken(accessToken);

        assertNotNull(extractedRoles);
        assertEquals(roles.size(), extractedRoles.size());
        assertTrue(extractedRoles.containsAll(roles));

        log.info("Extracted roles: {}", extractedRoles);
    }

    /**
     * 测试权限缓存
     */
    @Test
    public void testPermissionCache() {
        Long userId = 1001L;
        
        // 模拟权限数据
        // 实际应该从数据库加载
        // Set<String> authorities = jwtUtils.loadAndCacheUserAuthorities(userId);

        // 从缓存获取权限
        // Set<String> cachedAuthorities = jwtUtils.getUserAuthorities(userId);
        // assertNotNull(cachedAuthorities);

        // 清除缓存
        jwtUtils.evictUserAuthorities(userId);
        
        log.info("Permission cache test completed");
    }

    /**
     * 测试无效Token
     */
    @Test
    public void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        Jwt jwt = jwtUtils.validateAndParseToken(invalidToken);
        assertNull(jwt);

        Long userId = jwtUtils.getUserIdFromToken(invalidToken);
        assertNull(userId);

        log.info("Invalid token test passed");
    }
}
