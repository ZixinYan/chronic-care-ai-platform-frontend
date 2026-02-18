package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新用户角色请求
 */
@Data
public class UpdateUserRolesRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户账户ID
     */
    private Long userId;
    
    /**
     * 新的角色列表
     * 会替换用户现有的所有角色
     */
    private List<Integer> roleCodes;
}
