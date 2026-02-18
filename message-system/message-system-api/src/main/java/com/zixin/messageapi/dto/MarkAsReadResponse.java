package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标记已读响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MarkAsReadResponse extends BaseResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
}
