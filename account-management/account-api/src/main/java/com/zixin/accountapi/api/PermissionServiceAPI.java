package com.zixin.accountapi.api;

import com.zixin.accountapi.dto.GetUserPermissionRequest;
import com.zixin.accountapi.dto.GetUserPermissionResponse;
import com.zixin.accountapi.dto.HasPermissionRequest;
import com.zixin.accountapi.dto.HasPermissionResponse;

public interface PermissionServiceAPI {
    GetUserPermissionResponse getUserPermission(GetUserPermissionRequest request);

    HasPermissionResponse hasPermission(HasPermissionRequest request);
}
