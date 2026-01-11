package com.zixin.accountapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HasPermissionResponse extends BaseResponse {
    boolean hasPermission;
}
