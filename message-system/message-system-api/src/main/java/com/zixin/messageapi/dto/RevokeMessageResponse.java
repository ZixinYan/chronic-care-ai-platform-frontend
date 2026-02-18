package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 撤回消息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RevokeMessageResponse extends BaseResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
}
