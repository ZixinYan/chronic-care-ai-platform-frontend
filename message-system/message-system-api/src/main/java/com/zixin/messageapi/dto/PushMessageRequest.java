package com.zixin.messageapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 推送消息请求DTO (B端接口)
 * 
 * 用于B端系统推送消息给用户
 */
@Data
public class PushMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
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
     */
    private Long receiverId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
}
