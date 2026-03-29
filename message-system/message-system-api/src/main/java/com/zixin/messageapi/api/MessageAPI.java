package com.zixin.messageapi.api;

import com.zixin.messageapi.dto.*;

import java.util.List;

/**
 * 站内信服务API (Dubbo服务接口)
 * 
 * 作为B端服务,使用BaseResponse作为统一返回体
 */
public interface MessageAPI {
    
    /**
     * 发送消息(个人消息)
     *
     * @param senderId 发送者ID
     * @param request 发送消息请求
     * @return 发送消息响应
     */
    SendMessageResponse sendMessage(Long senderId, SendMessageRequest request);
    
    /**
     * 群发消息
     *
     * @param senderId 发送者ID
     * @param request 发送消息请求(包含receiverIds)
     * @return 发送消息响应
     */
    SendMessageResponse broadcastMessage(Long senderId, SendMessageRequest request);
    
    /**
     * 查询收件箱
     *
     * @param userId 用户ID
     * @param request 查询条件
     * @return 查询消息列表响应
     */
    QueryMessageResponse queryInbox(Long userId, QueryMessageRequest request);
    
    /**
     * 查询发件箱
     *
     * @param userId 用户ID
     * @param request 查询条件
     * @return 查询消息列表响应
     */
    QueryMessageResponse querySentBox(Long userId, QueryMessageRequest request);
    
    /**
     * 获取消息详情
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 获取消息详情响应
     */
    GetMessageDetailResponse getMessageDetail(Long userId, Long messageId);
    
    /**
     * 标记消息为已读
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 标记已读响应
     */
    MarkAsReadResponse markAsRead(Long userId, Long messageId);
    
    /**
     * 批量标记消息为已读
     *
     * @param userId 用户ID
     * @param messageIds 消息ID列表
     * @return 标记已读响应
     */
    MarkAsReadResponse batchMarkAsRead(Long userId, List<Long> messageIds);
    
    /**
     * 撤回消息
     * 只有发送者可以撤回,且在发送后一定时间内(如5分钟)
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 撤回消息响应
     */
    RevokeMessageResponse revokeMessage(Long userId, Long messageId);
    
    /**
     * 删除消息(软删除)
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 删除消息响应
     */
    DeleteMessageResponse deleteMessage(Long userId, Long messageId);
    
    /**
     * 批量删除消息
     *
     * @param userId 用户ID
     * @param messageIds 消息ID列表
     * @return 删除消息响应
     */
    DeleteMessageResponse batchDeleteMessage(Long userId, List<Long> messageIds);
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量响应
     */
    UnreadCountResponse getUnreadCount(Long userId);
    
    /**
     * 推送消息给单个用户 (B端接口)
     * 
     * 用于B端系统主动推送消息
     *
     * @param request 推送消息请求
     * @return 推送消息响应
     */
    PushMessageResponse pushMessage(PushMessageRequest request);
    
    /**
     * 批量推送消息给多个用户 (B端接口)
     * 
     * 用于B端系统批量推送消息
     *
     * @param request 批量推送消息请求
     * @return 批量推送消息响应
     */
    BatchPushMessageResponse batchPushMessage(BatchPushMessageRequest request);
}
