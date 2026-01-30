package com.zixin.accountapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 更新用户角色响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateUserRolesResponse extends BaseResponse {
    
    /**
     * 更新后的角色列表
     */
    private List<Integer> roleCodes;
}
