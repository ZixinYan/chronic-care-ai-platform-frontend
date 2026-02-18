package com.zixin.accountapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetUserPermissionResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    Set<String> permissions;
}
