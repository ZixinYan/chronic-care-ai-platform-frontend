package com.zixin.accountprovider.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.zixin.accountapi.api.PermissionServiceAPI;
import com.zixin.accountapi.dto.GetUserPermissionRequest;
import com.zixin.accountapi.dto.GetUserPermissionResponse;
import com.zixin.accountapi.dto.HasPermissionRequest;
import com.zixin.accountapi.dto.HasPermissionResponse;
import com.zixin.accountapi.po.RolePermission;
import com.zixin.accountprovider.mapper.UserRoleMapper;
import com.zixin.accountprovider.mapper.RolePermissionMapper;
import com.zixin.accountprovider.utils.PermissionExpandUtil;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DubboService
@Slf4j
public class PermissionServiceImpl implements PermissionServiceAPI {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionServiceImpl(UserRoleMapper userRoleMapper, RolePermissionMapper rolePermissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }


    @Override
    public GetUserPermissionResponse getUserPermission(GetUserPermissionRequest request) {
        Long userId = request.getUserId();
        GetUserPermissionResponse response = new GetUserPermissionResponse();
        try {
            // 1. 查询角色
            List<Integer> roleCodes =
                    userRoleMapper.selectRoleCodesByUserId(userId);

            if (CollectionUtils.isEmpty(roleCodes)) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("用户未分配角色");
                return response;
            }

            // 2. 查询权限
            List<RolePermission> permissions =
                    rolePermissionMapper.selectByRoleCodes(roleCodes);

            // 3. 权限展开
            Set<String> authorities = new HashSet<>();
            for (RolePermission permission : permissions) {
                authorities.addAll(
                        PermissionExpandUtil.expand(
                                permission.getRoleCode(),
                                permission.getActionCode()
                        )
                );
            }
            response.setPermissions(authorities);
            response.setCode(ToBCodeEnum.SUCCESS);
        }catch (Exception e){
            log.error("获取用户权限失败，userId={}，异常信息：{}",userId,e.getMessage(),e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("获取用户权限失败");
        }
        return response;
    }

    @Override
    public HasPermissionResponse hasPermission(HasPermissionRequest request) {
        GetUserPermissionRequest getUserPermissionRequest = new GetUserPermissionRequest();
        getUserPermissionRequest.setUserId(request.getUserId());
        GetUserPermissionResponse getUserPermissionResponse = getUserPermission(getUserPermissionRequest);
        HasPermissionResponse response = new HasPermissionResponse();

        Set<String> permissions = getUserPermissionResponse.getPermissions();
        response.setCode(ToBCodeEnum.SUCCESS);
        if(!permissions.contains(request.getPermission())){
            response.setHasPermission(false);
        }
        response.setHasPermission(true);
        return response;
    }
}
