package com.zixin.gateway.config;

import com.zixin.authprovider.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

/**
 * 网关全局过滤器
 * 1. 解析 JWT 获取 userId
 * 2. 从 Redis 获取权限缓存
 * 3. 注入 Header 下发下游
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceAndJwtAuthFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID = "X-Trace-Id";
    private static final String USER_ID = "X-User-Id";
    private static final String AUTHORITIES = "X-Authorities";

    private final JwtUtils jwtUtils;          // JWT 解析工具
    private final JwtUtils permissionCache; // Redis 权限缓存工具

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //  1. TraceId
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        String finalTraceId = traceId;

        // 2. JWT 校验
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        String token = authHeader.substring(7);
        Long userId;
        Set<String> authorities;

        try {
            // 解析 token
            userId = jwtUtils.getUserId(token);

            // 3. 从 Redis 获取权限
            authorities = permissionCache.getUserAuthorities(userId);
            if (authorities == null || authorities.isEmpty()) {
                log.warn("User permissions cache miss for userId {}", userId);
                // 可以调用 auth-provider 提供的 Dubbo API 拉权限并写入 Redis
                authorities = permissionCache.loadAndCacheUserAuthorities(userId);
            }

        } catch (Exception e) {
            log.error("[Gateway] JWT 校验或权限读取失败", e);
            return unauthorized(exchange);
        }

        String authoritiesHeader = String.join(",", authorities);

        // 4. 构建下游请求 Header
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder
                        .header(TRACE_ID, finalTraceId)
                        .header(USER_ID, String.valueOf(userId))
                        .header(AUTHORITIES, authoritiesHeader))
                .build();

        // 5. Reactor Context + MDC
        return chain.filter(mutated)
                .contextWrite(ctx -> ctx.put(TRACE_ID, finalTraceId)
                        .put(USER_ID, userId)
                        .put(AUTHORITIES, authoritiesHeader))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        MDC.put(TRACE_ID, finalTraceId);
                        MDC.put(USER_ID, String.valueOf(userId));
                    }
                })
                .doFinally(signal -> {
                    MDC.remove(TRACE_ID);
                    MDC.remove(USER_ID);
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
