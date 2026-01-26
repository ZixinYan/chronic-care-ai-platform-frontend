package com.zixin.authprovider.filter;

import com.zixin.authprovider.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT Token并验证
 * 验证成功后将用户信息设置到Spring Security上下文中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 从请求头获取Token
            String jwt = extractJwtFromRequest(request);

            // 2. 如果Token存在且有效，设置认证信息
            if (StringUtils.hasText(jwt)) {
                authenticateToken(jwt, request);
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // 清除认证上下文
            SecurityContextHolder.clearContext();
        }

        // 3. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT Token
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 验证Token并设置认证信息
     */
    private void authenticateToken(String token, HttpServletRequest request) {
        // 1. 验证并解析Token
        Jwt jwt = jwtUtils.validateAndParseToken(token);
        if (jwt == null) {
            log.warn("Invalid JWT token");
            return;
        }

        // 2. 检查Token类型(只允许Access Token)
        String tokenType = jwt.getClaim("type");
        if (!"access".equals(tokenType)) {
            log.warn("Invalid token type: {}", tokenType);
            return;
        }

        // 3. 提取用户信息
        String userId = jwt.getSubject();

        // 4. 提取权限信息
        List<GrantedAuthority> authorities = extractAuthorities(jwt);

        // 5. 创建认证对象
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 6. 设置到Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Set authentication for userId: {}, authorities: {}", userId, authorities);
    }

    /**
     * 从JWT中提取权限信息
     * 包括角色(roles)和权限(authorities)
     */
    @SuppressWarnings("unchecked")
    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // 1. 提取角色
        List<String> roles = jwt.getClaim("roles");
        if (roles != null) {
            grantedAuthorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        // 2. 提取权限
        List<String> authorities = jwt.getClaim("authorities");
        if (authorities != null) {
            grantedAuthorities.addAll(authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        return grantedAuthorities;
    }
}
