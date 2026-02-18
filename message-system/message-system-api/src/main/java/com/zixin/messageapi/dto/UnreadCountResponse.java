package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取未读数量响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnreadCountResponse extends BaseResponse {
    
    /**
     * 未读消息数量
     */
    private Long unreadCount;
}
