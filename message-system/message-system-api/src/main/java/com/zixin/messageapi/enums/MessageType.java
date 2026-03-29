package com.zixin.messageapi.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageType {
    
    /**
     * 系统通知
     */
    SYSTEM(1, "系统通知"),
    
    /**
     * 个人消息
     */
    PERSONAL(2, "个人消息"),
    
    /**
     * 群发消息
     */
    BROADCAST(3, "群发消息"),
    
    /**
     * 公告
     */
    ANNOUNCEMENT(4, "公告");
    
    private final int code;
    private final String description;
    
    MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static MessageType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}
