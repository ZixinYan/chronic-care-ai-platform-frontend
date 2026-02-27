package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 推送消息响应 (B端接口)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PushMessageResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 是否推送成功
     */
    private Boolean success;
}
