package com.zixin.authprovider.utils;

import com.zixin.authprovider.client.PermissionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 实现双Token机制(Access Token + Refresh Token)
 * - Access Token: 短期有效，用于API访问，包含用户ID、角色和权限
 * - Refresh Token: 长期有效，用于刷新Access Token，存储在Redis中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final StringRedisTemplate redisTemplate;
    private final PermissionClient permissionClient;

    // Redis Key前缀
    private static final String PERMISSION_KEY_PREFIX = "auth:permissions:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh_token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    // Access Token 有效期：30分钟
    @Value("${jwt.access-token.expiration:1800}")
    private long accessTokenExpiration;

    // Refresh Token 有效期：7天
    @Value("${jwt.refresh-token.expiration:604800}")
    private long refreshTokenExpiration;

    // 权限缓存有效期：30分钟
    @Value("${jwt.permissions.cache-ttl:1800}")
    private long permissionsCacheTtl;

    /**
     * 生成Access Token
     * 包含用户ID、用户名、角色和权限信息
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @param permissions 权限集合(直接传入,不再从数据库查询)
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, Set<String> permissions, String tokenId) {
        // 1. 构造 claims
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .id(tokenId)
                .expiresAt(expiresAt)
                .claim("type", "access");

        // 添加用户名
        if (username != null && !username.isEmpty()) {
            claimsBuilder.claim("username", username);
        }

        // 添加角色信息
        if (roles != null && !roles.isEmpty()) {
            claimsBuilder.claim("roles", roles);
        }

        // 添加权限信息(直接使用传入的permissions)
        if (permissions != null && !permissions.isEmpty()) {
            claimsBuilder.claim("authorities", new ArrayList<>(permissions));
            // 同时缓存到Redis供其他场景使用
            setUserAuthorities(userId, permissions);
        }

        JwtClaimsSet claimsSet = claimsBuilder.build();

        // 2. 生成 token
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
        log.debug("Generated access token for userId: {}, username: {}, roles: {}, permissions count: {}, expires at: {}", 
                userId, username, roles, permissions != null ? permissions.size() : 0, expiresAt);
        return token;
    }

    /**
     * 生成Refresh Token
     * 包含用户ID、角色和token ID，用于刷新Access Token
     *
     * @param userId 用户ID
     * @param roles 角色列表(用于刷新时重新生成Access Token)
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, List<String> roles, String tokenId) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(tokenId)
                .claim("type", "refresh");

        // 添加角色信息(用于刷新Token时使用)
        if (roles != null && !roles.isEmpty()) {
            claimsBuilder.claim("roles", roles);
        }

        JwtClaimsSet claimsSet = claimsBuilder.build();

        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

        // 将refresh token存储到Redis，用于验证
        String key = REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.SECONDS);

        log.debug("Generated refresh token for userId: {}, roles: {}, tokenId: {}, expires at: {}", 
                userId, roles, tokenId, expiresAt);
        return refreshToken;
    }

    /**
     * 生成Token对(Access Token + Refresh Token)
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @param permissions 权限集合
     * @return Map包含accessToken和refreshToken
     */
    public Map<String, String> generateTokenPair(Long userId, String username, List<String> roles, Set<String> permissions) {
        String tokenId = UUID.randomUUID().toString(); // 生成唯一的token ID
        String accessToken = generateAccessToken(userId, username, roles, permissions, tokenId);
        String refreshToken = generateRefreshToken(userId, roles, tokenId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        log.info("Generated token pair for userId: {}, username: {}, roles: {}, permissions count: {}", 
                userId, username, roles, permissions != null ? permissions.size() : 0);
        return tokens;
    }

    /**
     * 验证并解析Token
     *
     * @param token JWT Token
     * @return 解析后的JWT对象，如果验证失败返回null
     */
    public Jwt validateAndParseToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            // 检查token是否在黑名单中
            if (isTokenBlacklisted(jwt.getId())) {
                log.warn("Token is blacklisted: {}", jwt.getId());
                return null;
            }

            return jwt;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Refresh Token并返回用户ID
     *
     * @param refreshToken Refresh Token
     * @return 用户ID，验证失败返回null
     */
    public Long validateRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);

            // 验证token类型
            String type = jwt.getClaim("type");
            if (!"refresh".equals(type)) {
                log.warn("Invalid token type for refresh: {}", type);
                return null;
            }

            // 从Redis验证refresh token是否有效
            Long userId = Long.parseLong(jwt.getSubject());
            String tokenId = jwt.getId();
            String key = REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;

            String storedToken = redisTemplate.opsForValue().get(key);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                log.warn("Refresh token not found or mismatch in Redis for userId: {}, tokenId: {}", userId, tokenId);
                return null;
            }

            log.debug("Refresh token validated successfully for userId: {}", userId);
            return userId;
        } catch (JwtException | NumberFormatException e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 撤销Refresh Token
     *
     * @param userId 用户ID
     * @param tokenId Token ID
     */
    public void revokeRefreshToken(Long userId, String tokenId) {
        String key = REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;
        redisTemplate.delete(key);
        log.info("Revoked refresh token for userId: {}, tokenId: {}", userId, tokenId);
    }

    /**
     * 撤销用户的所有Refresh Token
     *
     * @param userId 用户ID
     */
    public void revokeAllRefreshTokens(Long userId) {
        String pattern = REFRESH_TOKEN_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Revoked all refresh tokens for userId: {}, count: {}", userId, keys.size());
        }
    }

    /**
     * 将Token加入黑名单(用于登出)
     *
     * @param tokenId Token ID
     * @param expiresAt 过期时间
     */
    public void blacklistToken(String tokenId, Instant expiresAt) {
        if (tokenId == null) {
            return;
        }

        String key = TOKEN_BLACKLIST_PREFIX + tokenId;
        long ttl = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
            log.info("Added token to blacklist: {}, ttl: {}s", tokenId, ttl);
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param tokenId Token ID
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        String key = TOKEN_BLACKLIST_PREFIX + tokenId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 从Redis获取用户权限
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    public Set<String> getUserAuthorities(Long userId) {
        String key = PERMISSION_KEY_PREFIX + userId;
        Set<String> authorities = redisTemplate.opsForSet().members(key);
        log.debug("Retrieved {} authorities from cache for userId: {}", 
                authorities != null ? authorities.size() : 0, userId);
        return authorities;
    }

    /**
     * 将权限写入Redis缓存
     *
     * @param userId 用户ID
     * @param authorities 权限集合
     */
    public void setUserAuthorities(Long userId, Set<String> authorities) {
        String key = PERMISSION_KEY_PREFIX + userId;
        redisTemplate.delete(key); // 先清空旧数据

        if (authorities != null && !authorities.isEmpty()) {
            redisTemplate.opsForSet().add(key, authorities.toArray(new String[0]));
            redisTemplate.expire(key, permissionsCacheTtl, TimeUnit.SECONDS);
            log.debug("Cached {} authorities for userId: {}, ttl: {}s", 
                    authorities.size(), userId, permissionsCacheTtl);
        }
    }

    /**
     * 从数据库加载权限并缓存
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    public Set<String> loadAndCacheUserAuthorities(Long userId) {
        Set<String> authorities = permissionClient.getPermissionsByUserId(userId);
        if (authorities != null && !authorities.isEmpty()) {
            setUserAuthorities(userId, authorities);
            log.info("Loaded and cached {} authorities for userId: {}", authorities.size(), userId);
        } else {
            authorities = new HashSet<>();
            log.warn("No authorities found for userId: {}", userId);
        }
        return authorities;
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    public void evictUserAuthorities(Long userId) {
        String key = PERMISSION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Evicted authorities cache for userId: {}", userId);
    }

    /**
     * 从Token获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，解析失败返回null
     */
    public Long getUserIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String subject = jwt.getSubject();
            return Long.parseLong(subject);
        } catch (JwtException | NumberFormatException e) {
            log.error("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token获取角色列表
     *
     * @param token JWT Token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaim("roles");
        } catch (JwtException e) {
            log.error("Failed to extract roles from token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 从Token获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaim("authorities");
        } catch (JwtException e) {
            log.error("Failed to extract authorities from token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 从Token获取用户名
     *
     * @param token JWT Token
     * @return 用户名，如果不存在返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaim("username");
        } catch (JwtException e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token获取Token ID
     */
    public String getTokenIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getId();
        } catch (JwtException e) {
            log.error("Failed to extract token ID from token: {}", e.getMessage());
            return null;
        }
    }
}
