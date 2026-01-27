package com.zixin.authconsumer.service;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.LoginResponse;
import com.zixin.authapi.api.LoginWithPhoneAPI;
import com.zixin.authconsumer.client.AccountClient;
import com.zixin.authconsumer.client.AuthClient;
import com.zixin.thirdpartyapi.dto.SendSMSRequest;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.Result;
import dto.GenTokenRequest;
import dto.GenTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SSO单点登录服务实现
 * 整合账户服务和认证服务，提供统一的登录接口
 */
@Service
@Slf4j
public class SSOServiceImpl implements LoginWithPhoneAPI {

    private final AccountClient accountClient;
    private final AuthClient authClient;

    public SSOServiceImpl(AccountClient accountClient, AuthClient authClient) {
        this.accountClient = accountClient;
        this.authClient = authClient;
    }

    @Override
    public Result login(LoginRequest loginRequest) {
        // 1. 校验参数
        if (loginRequest == null
                || loginRequest.getLoginAccount() == null
                || loginRequest.getLoginAccount().isEmpty()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isEmpty()) {
            log.warn("Invalid login request parameters");
            return Result.error("Invalid login request");
        }

        try {
            // 2. 调用账号服务进行登录验证
            LoginResponse loginResponse = accountClient.login(loginRequest);
            if (loginResponse.getCode() != ToBCodeEnum.SUCCESS) {
                log.warn("Account login failed: {}", loginResponse.getMessage());
                return Result.error(loginResponse.getMessage());
            }

            LoginResponse.LoginUserDTO loginUserDTO = loginResponse.getData();

            Long userId = loginUserDTO.getUserId();
            String username = loginUserDTO.getUsername();
            List<Integer> roleCodes = loginUserDTO.getRole();          // 角色code列表
            Set<String> permissions = loginUserDTO.getPermission();    // 权限code集合

            if (userId == null || username == null) {
                log.error("Login response invalid: userId or username is null");
                return Result.error("Login response invalid");
            }

            // 3. 将角色code转换为角色名称
            // 注意: 这里需要根据实际业务逻辑进行转换
            // 如果roleCodes是Integer类型，需要转换为String类型的角色名
            List<String> roleNames = convertRoleCodesToNames(roleCodes);

            // 4. 生成双Token (Access Token + Refresh Token)
            GenTokenRequest genTokenRequest = new GenTokenRequest();
            genTokenRequest.setUserId(userId);
            genTokenRequest.setUsername(username);
            genTokenRequest.setRoles(roleNames);  // 使用转换后的角色名称

            GenTokenResponse tokenResponse = authClient.generateToken(genTokenRequest);
            if (tokenResponse.getCode() != ToBCodeEnum.SUCCESS) {
                log.error("Failed to generate JWT: {}", tokenResponse.getMessage());
                return Result.error("Failed to generate JWT");
            }

            // 验证Token生成结果
            if (tokenResponse.getAccessToken() == null || tokenResponse.getRefreshToken() == null) {
                log.error("Token generation returned null tokens");
                return Result.error("Failed to generate JWT tokens");
            }

            // 5. 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("username", username);
            data.put("accessToken", tokenResponse.getAccessToken());
            data.put("refreshToken", tokenResponse.getRefreshToken());
            data.put("expiresIn", tokenResponse.getExpiresIn());
            data.put("tokenType", tokenResponse.getTokenType());
            data.put("roles", roleNames);
            data.put("permissions", permissions);

            log.info("User logged in successfully - userId: {}, username: {}", userId, username);
            return Result.success(data);

        } catch (Exception e) {
            log.error("Login failed with exception", e);
            return Result.error("Login failed: " + e.getMessage());
        }
    }

    /**
     * 将角色code转换为角色名称
     * 
     * @param roleCodes 角色code列表
     * @return 角色名称列表
     */
    private List<String> convertRoleCodesToNames(List<Integer> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 角色code到角色名称的映射
        // TODO: 这里应该从数据库或配置中获取映射关系
        Map<Integer, String> roleMapping = new HashMap<>();
        roleMapping.put(1, "ADMIN");
        roleMapping.put(2, "USER");
        roleMapping.put(3, "MANAGER");
        roleMapping.put(4, "DOCTOR");
        roleMapping.put(5, "PATIENT");

        return roleCodes.stream()
                .map(code -> roleMapping.getOrDefault(code, "USER"))
                .collect(Collectors.toList());
    }



    @Override
    public Result register(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Result sendCode(SendSMSRequest sendSMSRequest) {
        return null;
    }

}
