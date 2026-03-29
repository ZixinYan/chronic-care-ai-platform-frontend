package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetUserPermissionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
}
