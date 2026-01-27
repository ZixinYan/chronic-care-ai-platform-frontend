package com.zixin.gateway.config;

import com.zixin.authapi.api.TokenValidationAPI;
import dto.ValidateTokenRequest;
import dto.ValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * 网关JWT认证和链路追踪全局过滤器
 * 
 * 功能：
 * 1. 生成/传递 TraceId
 * 2. 验证JWT Token (通过Dubbo调用auth-server)
 * 3. 提取用户信息和权限
 * 4. 将用户信息注入到下游服务的请求头
 * 5. 白名单路径放行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TraceAndJwtAuthFilter implements GlobalFilter, Ordered {

    @DubboReference
    private TokenValidationAPI tokenValidationAPI;

    private static final String TRACE_ID = "X-Trace-Id";
    private static final String USER_ID = "X-User-Id";
    private static final String USER_ROLES = "X-User-Roles";
    private static final String USER_AUTHORITIES = "X-User-Authorities";
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径 - 这些路径不需要JWT认证
     */
    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/actuator/**",
            "/error"
    ));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        // 1. 生成/获取 TraceId
        String traceId = generateOrGetTraceId(exchange);
        String finalTraceId = traceId;
        
        // 2. 检查是否是白名单路径
        String path = exchange.getRequest().getPath().value();
        if (isWhiteListPath(path)) {
            log.debug("White list path: {}, skip JWT authentication", path);
            return continueWithTraceId(exchange, chain, finalTraceId);
        }
        
        // 3. 提取JWT Token
        String token = extractToken(exchange);
        if (token == null) {
            log.warn("Missing JWT token for path: {}", path);
            return unauthorized(exchange, "Missing authentication token");
        }
        
        // 4. 通过Dubbo调用auth-server验证Token
        return validateTokenAsync(token)
                .flatMap(response -> {
                    // 验证失败
                    if (response == null || !response.getValid()) {
                        log.warn("JWT validation failed for path: {}", path);
                        return unauthorized(exchange, "Invalid or expired token");
                    }
                    
                    // 验证成功，提取用户信息
                    Long userId = response.getUserId();
                    List<String> roles = response.getRoles();
                    List<String> authorities = response.getAuthorities();
                    
                    log.debug("JWT validated successfully - userId: {}, roles: {}, authorities: {}", 
                            userId, roles, authorities);
                    
                    // 5. 将用户信息注入到下游请求头
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                            .mutate()
                            .header(TRACE_ID, finalTraceId)
                            .header(USER_ID, String.valueOf(userId))
                            .header(USER_ROLES, roles != null ? String.join(",", roles) : "")
                            .header(USER_AUTHORITIES, authorities != null ? String.join(",", authorities) : "")
                            .build();
                    
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();
                    
                    // 6. 设置MDC和Reactor Context
                    return chain.filter(mutatedExchange)
                            .contextWrite(ctx -> ctx
                                    .put(TRACE_ID, finalTraceId)
                                    .put(USER_ID, userId)
                                    .put(USER_ROLES, roles)
                                    .put(USER_AUTHORITIES, authorities))
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
                })
                .onErrorResume(e -> {
                    log.error("Error during JWT validation", e);
                    return unauthorized(exchange, "Authentication failed");
                });
    }

    /**
     * 生成或获取TraceId
     */
    private String generateOrGetTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        return traceId;
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求头提取JWT Token
     */
    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 异步验证Token
     * 通过Dubbo调用auth-server的TokenValidationAPI
     */
    private Mono<ValidateTokenResponse> validateTokenAsync(String token) {
        return Mono.fromCallable(() -> {
            ValidateTokenRequest request = new ValidateTokenRequest();
            request.setToken(token);
            return tokenValidationAPI.validateToken(request);
        }).onErrorResume(e -> {
            log.error("Failed to validate token via Dubbo", e);
            return Mono.empty();
        });
    }

    /**
     * 白名单路径继续执行，只添加TraceId
     */
    private Mono<Void> continueWithTraceId(ServerWebExchange exchange, GatewayFilterChain chain, String traceId) {
        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header(TRACE_ID, traceId)
                .build();
        
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(mutatedExchange)
                .contextWrite(ctx -> ctx.put(TRACE_ID, traceId))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete()) {
                        MDC.put(TRACE_ID, traceId);
                    }
                })
                .doFinally(signal -> MDC.remove(TRACE_ID));
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized access: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // 设置为最高优先级，确保在其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
