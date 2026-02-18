package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 批量推送消息响应 (B端接口)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BatchPushMessageResponse extends BaseResponse {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 成功推送的数量
     */
    private Integer successCount;
    
    /**
     * 失败的接收者ID列表
     */
    private List<Long> failedReceiverIds;
}
