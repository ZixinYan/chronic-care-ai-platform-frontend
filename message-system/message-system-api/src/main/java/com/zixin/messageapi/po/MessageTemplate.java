package com.zixin.messageapi.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 消息模板实体类
 * 
 * 功能说明:
 * 1. 预定义消息模板,支持占位符替换
 * 2. 提高消息发送效率和一致性
 * 3. 便于统一管理消息格式
 * 
 * 使用场景:
 * - 健康预警通知
 * - 预约提醒
 * - 报告就绪通知
 * - 服药提醒
 * - 家属绑定通知
 * 
 * @author zixin
 */
@Data
@TableName("message_template")
public class MessageTemplate {
    
    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long templateId;
    
    /**
     * 模板编码 (唯一标识)
     * 例如: HEALTH_ALERT, APPOINTMENT_REMIND, REPORT_READY
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 消息类型
     * 关联MessageType枚举
     */
    private Integer messageType;
    
    /**
     * 标题模板
     * 支持占位符: {variableName}
     * 例如: "健康预警：{indicator}异常"
     */
    private String titleTemplate;
    
    /**
     * 内容模板
     * 支持占位符: {variableName}
     * 例如: "尊敬的{userName}，您的{indicator}数值为{value}"
     */
    private String contentTemplate;
    
    /**
     * 参数列表 (JSON格式)
     * 记录模板需要的参数名称
     * 例如: ["userName","indicator","value","alertLevel"]
     */
    private String params;
    
    /**
     * 是否启用
     * 0 - 禁用
     * 1 - 启用
     */
    private Integer isEnabled;
    
    /**
     * 备注说明
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
