package com.zixin.gateway.client;

import com.zixin.accountapi.api.PermissionServiceAPI;
import com.zixin.accountapi.dto.GetUserPermissionRequest;
import com.zixin.accountapi.dto.GetUserPermissionResponse;
import com.zixin.accountprovider.service.PermissionServiceImpl;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class PermissionClient {
    @DubboReference(check = false)
    private PermissionServiceAPI permissionService;

    public Set<String> getPermissionsByUserId(Long userId) {
        GetUserPermissionRequest getUserPermissionRequest = new GetUserPermissionRequest();
        getUserPermissionRequest.setUserId(userId);
        GetUserPermissionResponse getUserPermissionResponse = permissionService.getUserPermission(getUserPermissionRequest);
        if(getUserPermissionResponse.getCode().equals(ToBCodeEnum.FAIL)) {
            log.error("通过用户ID获取权限失败，userId：{}", userId);
            return null;
        }
        log.info("通过用户ID获取权限成功，userId：{}, permission:{}", userId, getUserPermissionResponse.getPermissions());
        return getUserPermissionResponse.getPermissions();
    }

}
