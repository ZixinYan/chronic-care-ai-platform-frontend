package com.zixin.authconsumer.service;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.LoginResponse;
import com.zixin.accountapi.dto.RegisterRequest;
import com.zixin.accountapi.dto.RegisterResponse;
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
            List<String> roleNames = convertRoleCodesToNames(roleCodes);

            // 4. 生成双Token (Access Token + Refresh Token)
            GenTokenRequest genTokenRequest = new GenTokenRequest();
            genTokenRequest.setUserId(userId);
            genTokenRequest.setUsername(username);
            genTokenRequest.setRoles(roleNames);
            genTokenRequest.setPermissions(permissions);

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
            data.put("tokenType", tokenResponse.getTokenType());


            log.info("User logged in successfully - userId: {}, username: {}, role name:{}, permission:{}", userId, username, roleNames, permissions);
            return Result.success(data);

        } catch (Exception e) {
            log.error("Login failed with exception", e);
            return Result.error("Login failed: " + e.getMessage());
        }
    }

    /**
     * 将角色code转换为角色名称
     * 使用RoleCode枚举进行转换
     * 
     * @param roleCodes 角色code列表 (1-DOCTOR, 2-PATIENT, 3-FAMILY)
     * @return 角色名称列表 (["DOCTOR", "PATIENT", "FAMILY"])
     */
    private List<String> convertRoleCodesToNames(List<Integer> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return new ArrayList<>();
        }

        return roleCodes.stream()
                .map(code -> {
                    try {
                        // 使用RoleCode枚举进行转换
                        return com.zixin.accountapi.enums.RoleCode.fromCode(code).name();
                    } catch (Exception e) {
                        log.warn("Unknown role code: {}, fallback to USER", code);
                        return "USER";
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Result register(RegisterRequest registerRequest) {
        // 1. 调用account服务进行注册
        RegisterResponse response;
        try {
            response = accountClient.register(registerRequest);
            if (response.getCode() != ToBCodeEnum.SUCCESS) {
                log.error("Account registration failed: {}", response.getMessage());
                return Result.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("Account registration failed with exception", e);
            return Result.error("Account registration failed: " + e.getMessage());
        }
        return Result.success().setData(response);
    }

    @Override
    public Result sendCode(SendSMSRequest sendSMSRequest) {
        return null;
    }

}
