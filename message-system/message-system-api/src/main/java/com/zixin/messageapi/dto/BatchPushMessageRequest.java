package com.zixin.messageapi.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量推送消息请求DTO (B端接口)
 * 
 * 用于B端系统批量推送消息给多个用户
 */
@Data
public class BatchPushMessageRequest {
    
    /**
     * 消息类型
     * 1 - SYSTEM (系统通知)
     * 3 - BROADCAST (群发消息)
     * 4 - ANNOUNCEMENT (公告)
     */
    private Integer messageType;
    
    /**
     * 接收者ID列表
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
