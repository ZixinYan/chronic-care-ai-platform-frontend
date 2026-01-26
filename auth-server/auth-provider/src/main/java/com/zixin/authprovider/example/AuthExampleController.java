package com.zixin.authprovider.example;

import com.zixin.authapi.api.JwtAPI;
import com.zixin.authapi.api.TokenValidationAPI;
import com.zixin.authprovider.annotation.RequirePermission;
import com.zixin.authprovider.annotation.RequireRole;
import dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * JWT认证示例Controller
 * 展示如何使用JWT双Token认证机制
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/example")
@RequiredArgsConstructor
public class AuthExampleController {

    @DubboReference
    private JwtAPI jwtAPI;

    @DubboReference
    private TokenValidationAPI tokenValidationAPI;

    /**
     * 示例1: 用户登录生成Token
     * 
     * 在实际应用中，这个接口应该在验证用户名密码后调用
     */
    @PostMapping("/login")
    public GenTokenResponse login(@RequestBody LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsername());

        // TODO: 验证用户名密码
        // User user = userService.validateCredentials(username, password);

        // 模拟用户数据
        Long userId = 1001L;
        String username = loginRequest.getUsername();
        
        // 构造Token生成请求
        GenTokenRequest request = new GenTokenRequest();
        request.setUserId(userId);
        request.setUsername(username);
        request.setRoles(Arrays.asList("USER", "ADMIN")); // 实际应该从数据库获取

        // 生成Token对
        GenTokenResponse response = jwtAPI.genToken(request);

        if (response.getCode().isSuccess()) {
            log.info("Login successful for user: {}, userId: {}", username, userId);
        } else {
            log.error("Login failed for user: {}, reason: {}", username, response.getMessage());
        }

        return response;
    }

    /**
     * 示例2: 刷新Token
     * 
     * 当Access Token过期时，使用Refresh Token获取新的Token对
     */
    @PostMapping("/refresh")
    public RefreshTokenResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        RefreshTokenResponse response = jwtAPI.refreshToken(request);

        if (response.getCode().isSuccess()) {
            log.info("Token refreshed successfully");
        } else {
            log.error("Token refresh failed: {}", response.getMessage());
        }

        return response;
    }

    /**
     * 示例3: 验证Token
     * 
     * 验证Token是否有效，并获取Token中的用户信息
     */
    @PostMapping("/validate")
    public ValidateTokenResponse validateToken(@RequestBody ValidateTokenRequest request) {
        log.info("Token validation attempt");

        ValidateTokenResponse response = tokenValidationAPI.validateToken(request);

        if (response.getValid()) {
            log.info("Token is valid, userId: {}, roles: {}", 
                    response.getUserId(), response.getRoles());
        } else {
            log.warn("Token validation failed: {}", response.getMessage());
        }

        return response;
    }

    /**
     * 示例4: 登出(撤销Token)
     * 
     * 将Token加入黑名单，使其失效
     */
    @PostMapping("/logout")
    public ValidateTokenResponse logout(@RequestBody ValidateTokenRequest request) {
        log.info("User logout attempt");

        ValidateTokenResponse response = tokenValidationAPI.revokeToken(request);

        if (response.getCode().isSuccess()) {
            log.info("Logout successful");
        } else {
            log.error("Logout failed: {}", response.getMessage());
        }

        return response;
    }

    /**
     * 示例5: 使用@RequireRole注解进行角色控制
     * 
     * 只有拥有ADMIN角色的用户才能访问
     */
    @GetMapping("/admin-only")
    @RequireRole("ADMIN")
    public String adminOnlyEndpoint() {
        log.info("Admin-only endpoint accessed");
        return "This endpoint requires ADMIN role";
    }

    /**
     * 示例6: 使用@RequireRole注解(多个角色，OR关系)
     * 
     * 拥有ADMIN或MANAGER角色之一即可访问
     */
    @GetMapping("/manager-area")
    @RequireRole(value = {"ADMIN", "MANAGER"}, requireAll = false)
    public String managerArea() {
        log.info("Manager area accessed");
        return "This endpoint requires ADMIN or MANAGER role";
    }

    /**
     * 示例7: 使用@RequireRole注解(多个角色，AND关系)
     * 
     * 必须同时拥有ADMIN和SUPER_USER角色才能访问
     */
    @GetMapping("/super-admin")
    @RequireRole(value = {"ADMIN", "SUPER_USER"}, requireAll = true)
    public String superAdminEndpoint() {
        log.info("Super admin endpoint accessed");
        return "This endpoint requires both ADMIN and SUPER_USER roles";
    }

    /**
     * 示例8: 使用@RequirePermission注解进行权限控制
     * 
     * 只有拥有user:write权限的用户才能访问
     */
    @PostMapping("/update-user")
    @RequirePermission("user:write")
    public String updateUser() {
        log.info("Update user endpoint accessed");
        return "User updated successfully";
    }

    /**
     * 示例9: 使用@RequirePermission注解(多个权限，AND关系)
     * 
     * 必须同时拥有user:read和user:write权限才能访问
     */
    @GetMapping("/manage-user")
    @RequirePermission(value = {"user:read", "user:write"}, requireAll = true)
    public String manageUser() {
        log.info("Manage user endpoint accessed");
        return "User management area";
    }

    /**
     * 示例10: 使用Spring Security的@PreAuthorize注解
     * 
     * 支持SpEL表达式进行复杂的权限控制
     */
    @GetMapping("/complex-permission")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('user:delete')")
    public String complexPermission() {
        log.info("Complex permission endpoint accessed");
        return "This endpoint requires ADMIN role and user:delete permission";
    }

    /**
     * 示例11: 公开接口(不需要认证)
     * 
     * 任何人都可以访问
     */
    @GetMapping("/public")
    public String publicEndpoint() {
        log.info("Public endpoint accessed");
        return "This is a public endpoint";
    }

    /**
     * 登录请求DTO
     */
    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
