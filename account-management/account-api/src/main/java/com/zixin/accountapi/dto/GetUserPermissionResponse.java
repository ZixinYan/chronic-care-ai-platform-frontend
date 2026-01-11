package com.zixin.accountapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetUserPermissionResponse extends BaseResponse {
    Set<String> permissions;
}
