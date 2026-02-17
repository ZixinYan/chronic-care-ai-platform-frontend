package com.zixin.authconsumer.client;

import com.zixin.accountapi.api.AccountManagementAPI;
import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.LoginResponse;
import com.zixin.accountapi.dto.RegisterRequest;
import com.zixin.accountapi.dto.RegisterResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class AccountClient {
    @DubboReference(check = false)
    private AccountManagementAPI accountManagementAPI;

    public LoginResponse login(LoginRequest loginRequest) {
        return accountManagementAPI.login(loginRequest);
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        return accountManagementAPI.register(registerRequest);
    }
}
