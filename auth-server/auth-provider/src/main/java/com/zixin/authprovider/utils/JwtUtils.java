package com.zixin.authprovider.utils;

import com.zixin.authprovider.client.PermissionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final StringRedisTemplate redisTemplate;
    private final PermissionClient permissionClient;

    private static final String PERMISSION_KEY_PREFIX = "ACCOUNT:PERMISSIONS:";
    static final long TTL_SECONDS = 1800; // 30分钟

    // token 有效期 30 分钟，可配置
    private static final long EXPIRATION_SECONDS = 1800;

    /**
     * 生成 JWT
     * JWT 只包含 userId
     * 权限从 Redis 获取
     */
    public String generateToken(Long userId) {

        // 1. 从缓存获取权限
        Set<String> authorities = getUserAuthorities(userId);

        // 2. 构造 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId.toString());
        claims.put("authorities", authorities);

        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .claims((Consumer<Map<String, Object>>) claims)
                .issuedAt(now)
                .expiresAt(now.plus(EXPIRATION_SECONDS, ChronoUnit.SECONDS))
                .build();

        // 3. 生成 token
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    /**
     * 从 Redis 获取权限
     */
    public Set<String> getUserAuthorities(Long userId) {
        String key = PERMISSION_KEY_PREFIX + userId;
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 将权限写入缓存
     */
    public void setUserAuthorities(Long userId, Set<String> authorities) {
        String key = PERMISSION_KEY_PREFIX + userId;
        redisTemplate.delete(key); // 先清空
        if (!authorities.isEmpty()) {
            redisTemplate.opsForSet().add(key, authorities.toArray(new String[0]));
            redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * 驱逐缓存
     */
    public void evictUserAuthorities(Long userId) {
        String key = PERMISSION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public Set<String> loadAndCacheUserAuthorities(Long userId) {
        Set<String> authorities = permissionClient.getPermissionsByUserId(userId);
        setUserAuthorities(userId, authorities); // 写入 Redis
        return authorities;
    }

    public Long getUserId(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String sub = jwt.getSubject(); // 我们在生成 token 时用 sub = userId
            return Long.parseLong(sub);
        } catch (JwtException | NumberFormatException e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
