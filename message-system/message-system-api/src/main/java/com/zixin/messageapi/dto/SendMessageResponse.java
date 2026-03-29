package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 发送消息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SendMessageResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 消息ID
     */
    private Long messageId;
}
