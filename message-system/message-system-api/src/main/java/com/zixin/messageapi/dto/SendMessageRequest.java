package com.zixin.messageapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 发送消息请求DTO
 */
@Data
@Builder
public class SendMessageRequest {
    
    /**
     * 消息类型
     * 1 - SYSTEM (系统通知)
     * 2 - PERSONAL (个人消息)
     * 3 - BROADCAST (群发消息)
     * 4 - ANNOUNCEMENT (公告)
     */
    private Integer messageType;
    
    /**
     * 接收者ID
     * 个人消息时必填
     * 群发消息时为null,使用receiverIds
     */
    private Long receiverId;
    
    /**
     * 接收者ID列表
     * 群发消息时必填
     */
    private List<Long> receiverIds;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
}
