package com.zixin.messageapi.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 群发消息接收者关联表
 * 
 * 用于记录群发消息的所有接收者及其阅读状态
 */
@Data
@TableName("message_recipient")
public class MessageRecipient {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 消息ID - 关联message_inbox表
     */
    private Long messageId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者名称
     */
    private String receiverName;
    
    /**
     * 消息状态 - 对应MessageStatus枚举
     * 0 - UNREAD (未读)
     * 1 - READ (已读)
     * 3 - DELETED (已删除)
     * 
     * 注意: 群发消息不支持接收者撤回,只有发送者可以撤回
     */
    private Integer status;
    
    /**
     * 阅读时间
     */
    private Long readTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Long createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
    
    /**
     * 删除标记 (0-未删除, 1-已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
