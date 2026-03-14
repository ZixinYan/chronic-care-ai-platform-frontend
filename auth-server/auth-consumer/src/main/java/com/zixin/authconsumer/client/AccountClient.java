package com.zixin.authconsumer.client;

import com.zixin.accountapi.api.AccountManagementAPI;
import com.zixin.accountapi.dto.*;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountClient {
    @DubboReference(timeout = 50000)
    private AccountManagementAPI accountManagementAPI;

    public LoginResponse login(LoginRequest loginRequest) {
        return accountManagementAPI.login(loginRequest);
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        return accountManagementAPI.register(registerRequest);
    }

    public boolean updateUserInfo(UpdateUserInfoRequest request) {
        UpdateUserInfoResponse response = accountManagementAPI.updateUserInfo(request);
        if(response.getCode().equals(ToBCodeEnum.SUCCESS)){
            return true;
        }else{
            log.error("Failed to update user info, code: {}, message: {}", response.getCode(), response.getMessage());
            return false;
        }
    }
}
