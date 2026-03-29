package com.zixin.messageapi.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum MessageStatus {
    
    /**
     * 未读
     */
    UNREAD(0, "未读"),
    
    /**
     * 已读
     */
    READ(1, "已读"),
    
    /**
     * 已撤回
     */
    REVOKED(2, "已撤回"),
    
    /**
     * 已删除(软删除)
     */
    DELETED(3, "已删除");
    
    private final int code;
    private final String description;
    
    MessageStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static MessageStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown message status code: " + code);
    }
}
