package com.zixin.authconsumer.client;

import com.zixin.accountapi.dto.LoginRequest;
import com.zixin.accountapi.dto.LoginResponse;
import com.zixin.accountapi.dto.RegisterRequest;
import com.zixin.accountapi.dto.RegisterResponse;
import com.zixin.accountprovider.service.AccountServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class AccountClient {
    @DubboReference
    private AccountServiceImpl accountService;

    public LoginResponse login(LoginRequest loginRequest) {
        return accountService.login(loginRequest);
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        return accountService.register(registerRequest);
    }
}
