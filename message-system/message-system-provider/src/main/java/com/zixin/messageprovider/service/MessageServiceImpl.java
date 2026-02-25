package com.zixin.messageprovider.service;

import com.zixin.accountapi.dto.GetUserInfoResponse;
import com.zixin.messageapi.api.MessageAPI;
import com.zixin.messageapi.dto.*;
import com.zixin.messageapi.enums.MessageStatus;
import com.zixin.messageapi.enums.MessageType;
import com.zixin.messageapi.po.Message;
import com.zixin.messageapi.vo.MessageVO;
import com.zixin.messageprovider.client.AccountClient;
import com.zixin.messageprovider.mapper.MessageMapper;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内信服务实现类 (Dubbo服务)
 */
@Service
@DubboService
@Slf4j
public class MessageServiceImpl implements MessageAPI {

    private final MessageMapper messageMapper;
    private final AccountClient accountClient;
    
    // 消息撤回时间限制(5分钟)
    private static final long REVOKE_TIME_LIMIT = 5 * 60 * 1000;

    public MessageServiceImpl(MessageMapper messageMapper, AccountClient accountClient) {
        this.messageMapper = messageMapper;
        this.accountClient = accountClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        SendMessageResponse response = new SendMessageResponse();

        try {
            // 参数验证
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息标题不能为空");
                return response;
            }
            
            // 创建消息
            Message message = new Message();
            message.setMessageType(request.getMessageType() != null ? 
                    request.getMessageType() : MessageType.PERSONAL.getCode());
            message.setSenderId(senderId);
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setStatus(MessageStatus.UNREAD.getCode());
            message.setIsBroadcast(0);
            message.setCreateTime(System.currentTimeMillis());
            message.setUpdateTime(System.currentTimeMillis());
            if(request.getSenderName() != null) {
                message.setSenderName(request.getSenderName());
            }else{
                message.setSenderName(UserInfoManager.getUsername());
            }

            if(request.getReceiverId() != null){
                // 单插消息
                message.setReceiverId(request.getReceiverId());
                List<Long> receiverIds = new ArrayList<>();
                receiverIds.add(request.getReceiverId());
                message.setReceiverName(accountClient.getUserInfo(receiverIds).get(0).getUsername());
            }else{
                log.error("ReceiverId is null for personal message, senderId: {}", senderId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("接收者ID不能为空");
                return response;
            }
            // 插入数据库
            int rows = messageMapper.insert(message);
            if (rows > 0) {
                log.info("Send message success, messageId: {}, senderId: {}, receiverId: {}", 
                        message.getMessageId(), senderId, request.getReceiverId());
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("发送消息成功");
                response.setMessageId(message.getMessageId());
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("发送消息失败");
            }
        } catch (Exception e) {
            log.error("Send message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("发送消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse broadcastMessage(Long senderId, SendMessageRequest request) {
        SendMessageResponse response = new SendMessageResponse();
        
        try {
            if (request.getReceiverIds() == null || request.getReceiverIds().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("接收者列表不能为空");
                return response;
            }
            
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息标题不能为空");
                return response;
            }
            
            // 为每个接收者创建一条消息记录
            Long groupMessageId = null;  // 用于标识同一群发消息组
            int successCount = 0;
            long currentTime = System.currentTimeMillis();

            List<GetUserInfoResponse.UserInfoDTO> user = accountClient.getUserInfo(request.getReceiverIds());
            Map<Long, GetUserInfoResponse.UserInfoDTO> userHandler = user.stream().collect(Collectors.toMap(GetUserInfoResponse.UserInfoDTO::getUserId, u -> u));
            
            for (Long receiverId : request.getReceiverIds()) {
                Message message = new Message();
                message.setMessageType(MessageType.BROADCAST.getCode());
                message.setSenderId(UserInfoManager.getUserId());
                message.setSenderName(UserInfoManager.getUsername());
                message.setReceiverId(receiverId);  // 为每个接收者创建记录
                message.setReceiverName(userHandler.get(receiverId) != null ? userHandler.get(receiverId).getUsername() : "未知用户");
                message.setTitle(request.getTitle());
                message.setContent(request.getContent());
                message.setStatus(MessageStatus.UNREAD.getCode());
                message.setIsBroadcast(1);
                message.setCreateTime(currentTime);
                message.setUpdateTime(currentTime);
                
                // 设置群发消息组ID（第一条消息的ID作为组ID）
                if (groupMessageId == null) {
                    int rows = messageMapper.insert(message);
                    if (rows > 0) {
                        groupMessageId = message.getMessageId();
                        message.setGroupMessageId(groupMessageId);
                        // 更新第一条消息的group_message_id为自己
                        messageMapper.updateById(message);
                        successCount++;
                    }
                } else {
                    message.setGroupMessageId(groupMessageId);
                    int rows = messageMapper.insert(message);
                    if (rows > 0) {
                        successCount++;
                    }
                }
            }
            
            if (successCount == 0) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("创建群发消息失败");
                return response;
            }
            
            log.info("Broadcast message success, groupMessageId: {}, senderId: {}, recipients count: {}", 
                    groupMessageId, UserInfoManager.getUserId(), successCount);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("群发消息成功");
            response.setMessageId(groupMessageId);
        } catch (Exception e) {
            log.error("Broadcast message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("群发消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public QueryMessageResponse queryInbox(Long userId, QueryMessageRequest request) {
        QueryMessageResponse response = new QueryMessageResponse();
        
        try {
            if (userId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("用户ID不能为空");
                return response;
            }
            
            // 统一查询所有消息（个人消息和群发消息）
            List<Message> messages = messageMapper.selectInboxMessages(
                    userId, 
                    request.getMessageType(), 
                    request.getUnreadOnly() != null && request.getUnreadOnly() ? 
                            MessageStatus.UNREAD.getCode() : request.getStatus()
            );
            
            // 转换为VO
            List<MessageVO> result = messages.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            log.info("Query inbox success, userId: {}, count: {}", userId, result.size());
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询收件箱成功");
            response.setMessages(result);
            response.setTotal((long) result.size());
        } catch (Exception e) {
            log.error("Query inbox error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询收件箱异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public QueryMessageResponse querySentBox(Long userId, QueryMessageRequest request) {
        QueryMessageResponse response = new QueryMessageResponse();
        
        try {
            if (userId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("用户ID不能为空");
                return response;
            }
            
            // 查询发送的消息
            List<Message> messages = messageMapper.selectSentMessages(
                    userId, 
                    request.getMessageType(), 
                    request.getStatus()
            );
            
            List<MessageVO> result = messages.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            log.info("Query sent box success, userId: {}, count: {}", userId, result.size());
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询发件箱成功");
            response.setMessages(result);
            response.setTotal((long) result.size());
        } catch (Exception e) {
            log.error("Query sent box error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询发件箱异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetMessageDetailResponse getMessageDetail(Long userId, Long messageId) {
        GetMessageDetailResponse response = new GetMessageDetailResponse();
        
        try {
            if (userId == null || messageId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息不存在");
                return response;
            }
            
            // 验证权限：只有发送者或接收者可以查看
            if (!userId.equals(message.getSenderId()) && !userId.equals(message.getReceiverId())) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权限查看该消息");
                return response;
            }
            
            MessageVO vo = convertToVO(message);
            log.info("Get message detail success, messageId: {}, userId: {}", messageId, userId);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("获取消息详情成功");
            response.setMessageVO(vo);
        } catch (Exception e) {
            log.error("Get message detail error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("获取消息详情异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarkAsReadResponse markAsRead(Long userId, Long messageId) {
        MarkAsReadResponse response = new MarkAsReadResponse();
        
        try {
            if (userId == null || messageId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息不存在");
                return response;
            }
            
            // 验证权限：只有接收者可以标记消息为已读
            if (!userId.equals(message.getReceiverId())) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("只有接收者可以标记消息为已读");
                return response;
            }
            
            // 统一更新消息状态
            message.setStatus(MessageStatus.READ.getCode());
            message.setReadTime(System.currentTimeMillis());
            int rows = messageMapper.updateById(message);
            log.info("Mark as read success, messageId: {}, userId: {}", messageId, userId);
            response.setSuccess(rows > 0);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("标记已读成功");
        } catch (Exception e) {
            log.error("Mark as read error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("标记已读异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarkAsReadResponse batchMarkAsRead(Long userId, List<Long> messageIds) {
        MarkAsReadResponse response = new MarkAsReadResponse();
        
        try {
            if (userId == null || messageIds == null || messageIds.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            // 批量更新消息状态
            long currentTime = System.currentTimeMillis();
            int rows = messageMapper.batchUpdateStatus(
                    messageIds, 
                    userId, 
                    MessageStatus.READ.getCode(), 
                    currentTime,
                    currentTime
            );
            
            log.info("Batch mark as read success, userId: {}, count: {}, updated: {}", 
                    userId, messageIds.size(), rows);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量标记已读成功");
            response.setSuccess(rows > 0);
        } catch (Exception e) {
            log.error("Batch mark as read error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("批量标记已读异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RevokeMessageResponse revokeMessage(Long userId, Long messageId) {
        RevokeMessageResponse response = new RevokeMessageResponse();
        
        try {
            if (userId == null || messageId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息不存在");
                return response;
            }
            
            // 只有发送者可以撤回
            if (!userId.equals(message.getSenderId())) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("只有发送者可以撤回消息");
                return response;
            }
            
            // 检查是否在撤回时间限制内
            long timeDiff = System.currentTimeMillis() - message.getCreateTime();
            if (timeDiff > REVOKE_TIME_LIMIT) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息发送超过5分钟，无法撤回");
                return response;
            }
            
            // 更新消息状态
            message.setStatus(MessageStatus.REVOKED.getCode());
            message.setRevokeTime(System.currentTimeMillis());
            int rows = messageMapper.updateById(message);
            
            log.info("Revoke message success, messageId: {}, userId: {}", messageId, userId);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("撤回消息成功");
            response.setSuccess(rows > 0);
        } catch (Exception e) {
            log.error("Revoke message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("撤回消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteMessageResponse deleteMessage(Long userId, Long messageId) {
        DeleteMessageResponse response = new DeleteMessageResponse();
        
        try {
            if (userId == null || messageId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息不存在");
                return response;
            }
            
            // 验证权限：只有发送者或接收者可以删除
            if (!userId.equals(message.getSenderId()) && !userId.equals(message.getReceiverId())) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权限删除该消息");
                return response;
            }
            
            // 统一软删除
            int rows = messageMapper.deleteById(messageId);
            log.info("Delete message success, messageId: {}, userId: {}", messageId, userId);
            response.setSuccess(rows > 0);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("删除消息成功");
        } catch (Exception e) {
            log.error("Delete message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("删除消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteMessageResponse batchDeleteMessage(Long userId, List<Long> messageIds) {
        DeleteMessageResponse response = new DeleteMessageResponse();
        
        try {
            if (userId == null || messageIds == null || messageIds.isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("参数不能为空");
                return response;
            }
            
            // 批量删除消息（软删除）
            long currentTime = System.currentTimeMillis();
            int rows = messageMapper.batchDeleteByIds(messageIds, userId, currentTime);
            
            log.info("Batch delete message success, userId: {}, count: {}, deleted: {}", 
                    userId, messageIds.size(), rows);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量删除消息成功");
            response.setSuccess(rows > 0);
        } catch (Exception e) {
            log.error("Batch delete message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("批量删除消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        UnreadCountResponse response = new UnreadCountResponse();
        
        try {
            if (userId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("用户ID不能为空");
                return response;
            }
            
            // 统一统计未读消息数量
            Long totalCount = messageMapper.countUnreadMessages(userId);
            if (totalCount == null) {
                totalCount = 0L;
            }
            
            log.info("Get unread count success, userId: {}, count: {}", userId, totalCount);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("获取未读数量成功");
            response.setUnreadCount(totalCount);
        } catch (Exception e) {
            log.error("Get unread count error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("获取未读数量异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PushMessageResponse pushMessage(PushMessageRequest request) {
        PushMessageResponse response = new PushMessageResponse();
        
        try {
            // 参数验证
            if (request.getReceiverId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("接收者不能为空");
                response.setSuccess(false);
                return response;
            }
            
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息标题不能为空");
                response.setSuccess(false);
                return response;
            }
            
            // 创建消息(系统推送,senderId为null)
            Message message = new Message();
            message.setMessageType(request.getMessageType() != null ? 
                    request.getMessageType() : MessageType.SYSTEM.getCode());
            message.setSenderId(0L);  // 系统推送消息,senderId为0
            message.setSenderName("系统");
            message.setReceiverId(request.getReceiverId());
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setStatus(MessageStatus.UNREAD.getCode());
            message.setIsBroadcast(0);
            message.setCreateTime(System.currentTimeMillis());
            message.setUpdateTime(System.currentTimeMillis());
            
            // 插入数据库
            int rows = messageMapper.insert(message);
            if (rows > 0) {
                log.info("Push message success, messageId: {}, receiverId: {}", 
                        message.getMessageId(), request.getReceiverId());
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("推送消息成功");
                response.setMessageId(message.getMessageId());
                response.setSuccess(true);
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("推送消息失败");
                response.setSuccess(false);
            }
        } catch (Exception e) {
            log.error("Push message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("推送消息异常: " + e.getMessage());
            response.setSuccess(false);
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchPushMessageResponse batchPushMessage(BatchPushMessageRequest request) {
        BatchPushMessageResponse response = new BatchPushMessageResponse();
        
        try {
            // 参数验证
            if (request.getReceiverIds() == null || request.getReceiverIds().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("接收者列表不能为空");
                return response;
            }
            
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息标题不能为空");
                return response;
            }
            
            // 为每个接收者创建一条消息记录
            Long groupMessageId = null;  // 用于标识同一群发消息组
            int successCount = 0;
            long currentTime = System.currentTimeMillis();
            
            for (Long receiverId : request.getReceiverIds()) {
                Message message = new Message();
                message.setMessageType(request.getMessageType() != null ? 
                        request.getMessageType() : MessageType.BROADCAST.getCode());
                message.setSenderId(0L);  // 系统推送消息,senderId为0
                message.setSenderName("系统");
                message.setReceiverId(receiverId);  // 为每个接收者创建记录
                message.setTitle(request.getTitle());
                message.setContent(request.getContent());
                message.setStatus(MessageStatus.UNREAD.getCode());
                message.setIsBroadcast(1);
                message.setCreateTime(currentTime);
                message.setUpdateTime(currentTime);
                
                // 设置群发消息组ID（第一条消息的ID作为组ID）
                if (groupMessageId == null) {
                    int rows = messageMapper.insert(message);
                    if (rows > 0) {
                        groupMessageId = message.getMessageId();
                        message.setGroupMessageId(groupMessageId);
                        // 更新第一条消息的group_message_id为自己
                        messageMapper.updateById(message);
                        successCount++;
                    }
                } else {
                    message.setGroupMessageId(groupMessageId);
                    int rows = messageMapper.insert(message);
                    if (rows > 0) {
                        successCount++;
                    }
                }
            }
            
            if (successCount == 0) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("创建批量推送消息失败");
                return response;
            }
            
            log.info("Batch push message success, groupMessageId: {}, recipients count: {}", 
                    groupMessageId, successCount);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量推送消息成功");
            response.setMessageId(groupMessageId);
            response.setSuccessCount(successCount);
            response.setFailedReceiverIds(new ArrayList<>());
        } catch (Exception e) {
            log.error("Batch push message error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("批量推送消息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        
        // 设置类型描述
        try {
            MessageType type = MessageType.fromCode(message.getMessageType());
            vo.setMessageTypeDesc(type.getDescription());
        } catch (Exception e) {
            vo.setMessageTypeDesc("未知");
        }
        
        // 设置状态描述
        try {
            MessageStatus status = MessageStatus.fromCode(message.getStatus());
            vo.setStatusDesc(status.getDescription());
        } catch (Exception e) {
            vo.setStatusDesc("未知");
        }
        
        return vo;
    }
}
