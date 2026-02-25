package com.zixin.messageprovider.client;

import com.zixin.accountapi.api.AccountManagementAPI;
import com.zixin.accountapi.dto.GetUserInfoRequest;
import com.zixin.accountapi.dto.GetUserInfoResponse;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AccountClient {
    @DubboReference(timeout = 50000)
    private AccountManagementAPI accountManagementAPI;

    public List<GetUserInfoResponse.UserInfoDTO> getUserInfo(List<Long> userIds) {
        GetUserInfoRequest request = new GetUserInfoRequest();
        request.setUserIds(userIds);
        GetUserInfoResponse response = accountManagementAPI.getUserInfo(request);
        if(response.getCode().equals(ToBCodeEnum.FAIL)){
            log.error("Failed to get user info for userIds {}: {}", userIds, response.getMessage());
            return new ArrayList<>();
        }

        return response.getUsers();
    }
}
