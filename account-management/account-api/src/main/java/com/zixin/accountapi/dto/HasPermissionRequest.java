package com.zixin.accountapi.dto;

import lombok.Data;

@Data
public class HasPermissionRequest {
    private Long userId;
    private String permission;
}
