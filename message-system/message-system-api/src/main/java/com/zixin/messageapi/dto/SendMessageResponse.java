package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送消息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SendMessageResponse extends BaseResponse {
    
    /**
     * 消息ID
     */
    private Long messageId;
}
