package com.zixin.accountapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class HasPermissionResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    boolean hasPermission;
}
