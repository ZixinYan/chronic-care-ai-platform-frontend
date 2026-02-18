package com.zixin.messageapi.dto;

import lombok.Data;

/**
 * 消息查询请求DTO
 */
@Data
public class QueryMessageRequest {
    
    /**
     * 消息类型
     */
    private Integer messageType;
    
    /**
     * 消息状态
     */
    private Integer status;
    
    /**
     * 是否只查询未读消息
     */
    private Boolean unreadOnly;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
