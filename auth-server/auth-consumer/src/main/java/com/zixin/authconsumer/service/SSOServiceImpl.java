package com.zixin.authconsumer.service;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.LoginResponse;
import com.zixin.accountprovider.service.AccountServiceImpl;
import com.zixin.authapi.api.LoginWithPhoneAPI;
import com.zixin.authconsumer.client.AccountClient;
import com.zixin.authconsumer.client.AuthClient;
import com.zixin.thirdpartyapi.dto.SendSMSRequest;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.Result;
import dto.GenTokenRequest;
import dto.GenTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            return Result.error("Invalid login request");
        }

        try {
            // 2. 调用账号服务进行登录
            LoginResponse loginResponse = accountClient.login(loginRequest);
            if (loginResponse.getCode() != ToBCodeEnum.SUCCESS) {
                return Result.error(loginResponse.getMessage());
            }

            LoginResponse.LoginUserDTO loginUserDTO = loginResponse.getData();

            Long userId = loginUserDTO.getUserId();
            String username = loginUserDTO.getUsername();
            List<Integer> roles = loginUserDTO.getRole();          // 正确获取角色 code
            Set<String> permissions = loginUserDTO.getPermission(); // 权限 code

            if (userId == null || username == null) {
                return Result.error("Login response invalid");
            }

            // 3. 生成 JWT
            GenTokenRequest genTokenRequest = new GenTokenRequest();
            genTokenRequest.setUserId(userId);
            genTokenRequest.setUsername(username);
            genTokenRequest.setRoles(roles);
            genTokenRequest.setPermissions(permissions);

            GenTokenResponse tokenResponse = authClient.getToken(genTokenRequest);
            if (tokenResponse.getCode() != ToBCodeEnum.SUCCESS || tokenResponse.getToken() == null) {
                return Result.error("Failed to generate JWT");
            }

            String jwt = tokenResponse.getToken();

            // 4. 构建返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("username", username);
            data.put("token", jwt);
            data.put("roles", roles);
            data.put("permissions", permissions);

            return Result.success(data);

        } catch (Exception e) {
            log.error("Login failed", e);
            return Result.error("Login failed: " + e.getMessage());
        }
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
