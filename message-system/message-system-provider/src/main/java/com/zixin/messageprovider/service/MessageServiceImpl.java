package com.zixin.messageprovider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zixin.messageapi.api.MessageAPI;
import com.zixin.messageapi.dto.*;
import com.zixin.messageapi.enums.MessageStatus;
import com.zixin.messageapi.enums.MessageType;
import com.zixin.messageapi.po.Message;
import com.zixin.messageapi.po.MessageRecipient;
import com.zixin.messageapi.vo.MessageVO;
import com.zixin.messageprovider.mapper.MessageMapper;
import com.zixin.messageprovider.mapper.MessageRecipientMapper;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内信服务实现类 (Dubbo服务)
 * 
 * 只提供Dubbo RPC接口,不暴露HTTP Controller
 * 通过Dubbo被Consumer调用
 */
@Service
@DubboService
@Slf4j
public class MessageServiceImpl implements MessageAPI {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private MessageRecipientMapper messageRecipientMapper;
    
    // 消息撤回时间限制(5分钟)
    private static final long REVOKE_TIME_LIMIT = 5 * 60 * 1000;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        SendMessageResponse response = new SendMessageResponse();
        
        try {
            // 参数验证
            if (senderId == null || request.getReceiverId() == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("发送者和接收者不能为空");
                return response;
            }
            
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
            message.setReceiverId(request.getReceiverId());
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setStatus(MessageStatus.UNREAD.getCode());
            message.setIsBroadcast(0);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
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
            // 参数验证
            if (senderId == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("发送者不能为空");
                return response;
            }
            
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
            
            // 创建群发消息
            Message message = new Message();
            message.setMessageType(MessageType.BROADCAST.getCode());
            message.setSenderId(senderId);
            message.setReceiverId(null);  // 群发消息receiverId为null
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setStatus(MessageStatus.UNREAD.getCode());
            message.setIsBroadcast(1);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
            // 插入消息
            int rows = messageMapper.insert(message);
            if (rows <= 0) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("创建群发消息失败");
                return response;
            }
            
            // 创建接收者记录
            List<MessageRecipient> recipients = request.getReceiverIds().stream()
                    .map(receiverId -> {
                        MessageRecipient recipient = new MessageRecipient();
                        recipient.setMessageId(message.getMessageId());
                        recipient.setReceiverId(receiverId);
                        recipient.setStatus(MessageStatus.UNREAD.getCode());
                        recipient.setCreateTime(new Date());
                        recipient.setUpdateTime(new Date());
                        return recipient;
                    })
                    .collect(Collectors.toList());
            
            // 批量插入接收者
            int recipientRows = messageRecipientMapper.batchInsert(recipients);
            log.info("Broadcast message success, messageId: {}, senderId: {}, recipients count: {}", 
                    message.getMessageId(), senderId, recipientRows);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("群发消息成功");
            response.setMessageId(message.getMessageId());
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
            
            // 查询个人消息
            List<Message> personalMessages = messageMapper.selectInboxMessages(
                    userId, 
                    request.getMessageType(), 
                    request.getUnreadOnly() != null && request.getUnreadOnly() ? 
                            MessageStatus.UNREAD.getCode() : request.getStatus()
            );
            
            List<MessageVO> result = new ArrayList<>();
            
            // 转换个人消息
            for (Message message : personalMessages) {
                result.add(convertToVO(message));
            }
            
            // 查询群发消息
            List<MessageRecipient> recipients = messageRecipientMapper.selectByReceiverId(
                    userId,
                    request.getUnreadOnly() != null && request.getUnreadOnly() ? 
                            MessageStatus.UNREAD.getCode() : request.getStatus()
            );
            
            // 获取群发消息详情
            for (MessageRecipient recipient : recipients) {
                Message message = messageMapper.selectById(recipient.getMessageId());
                if (message != null) {
                    MessageVO vo = convertToVO(message);
                    vo.setStatus(recipient.getStatus());  // 使用接收者的状态
                    vo.setReadTime(recipient.getReadTime());
                    result.add(vo);
                }
            }
            
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
            
            // 验证权限(是发送者或接收者)
            if (!userId.equals(message.getSenderId()) && !userId.equals(message.getReceiverId())) {
                // 检查是否是群发消息的接收者
                if (message.getIsBroadcast() == 1) {
                    LambdaQueryWrapper<MessageRecipient> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(MessageRecipient::getMessageId, messageId)
                           .eq(MessageRecipient::getReceiverId, userId);
                    MessageRecipient recipient = messageRecipientMapper.selectOne(wrapper);
                    if (recipient == null) {
                        response.setCode(ToBCodeEnum.FAIL);
                        response.setMessage("无权限查看该消息");
                        return response;
                    }
                } else {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("无权限查看该消息");
                    return response;
                }
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
            
            // 检查是否是群发消息
            if (message.getIsBroadcast() == 1) {
                // 更新群发消息接收者的状态
                LambdaUpdateWrapper<MessageRecipient> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(MessageRecipient::getMessageId, messageId)
                       .eq(MessageRecipient::getReceiverId, userId)
                       .set(MessageRecipient::getStatus, MessageStatus.READ.getCode())
                       .set(MessageRecipient::getReadTime, new Date());
                int rows = messageRecipientMapper.update(null, wrapper);
                response.setSuccess(rows > 0);
            } else {
                // 更新个人消息状态
                if (!userId.equals(message.getReceiverId())) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("只有接收者可以标记消息为已读");
                    return response;
                }
                
                message.setStatus(MessageStatus.READ.getCode());
                message.setReadTime(new Date());
                int rows = messageMapper.updateById(message);
                log.info("Mark as read success, messageId: {}, userId: {}", messageId, userId);
                response.setSuccess(rows > 0);
            }
            
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
            
            for (Long messageId : messageIds) {
                markAsRead(userId, messageId);
            }
            
            log.info("Batch mark as read success, userId: {}, count: {}", userId, messageIds.size());
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量标记已读成功");
            response.setSuccess(true);
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
            long timeDiff = System.currentTimeMillis() - message.getCreateTime().getTime();
            if (timeDiff > REVOKE_TIME_LIMIT) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("消息发送超过5分钟，无法撤回");
                return response;
            }
            
            // 更新消息状态
            message.setStatus(MessageStatus.REVOKED.getCode());
            message.setRevokeTime(new Date());
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
            
            // 检查是否是群发消息
            if (message.getIsBroadcast() == 1) {
                // 删除群发消息接收者记录(软删除)
                LambdaUpdateWrapper<MessageRecipient> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(MessageRecipient::getMessageId, messageId)
                       .eq(MessageRecipient::getReceiverId, userId)
                       .set(MessageRecipient::getStatus, MessageStatus.DELETED.getCode());
                int rows = messageRecipientMapper.update(null, wrapper);
                response.setSuccess(rows > 0);
            } else {
                // 验证权限
                if (!userId.equals(message.getSenderId()) && !userId.equals(message.getReceiverId())) {
                    response.setCode(ToBCodeEnum.FAIL);
                    response.setMessage("无权限删除该消息");
                    return response;
                }
                
                // 软删除
                int rows = messageMapper.deleteById(messageId);
                log.info("Delete message success, messageId: {}, userId: {}", messageId, userId);
                response.setSuccess(rows > 0);
            }
            
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
            
            for (Long messageId : messageIds) {
                deleteMessage(userId, messageId);
            }
            
            log.info("Batch delete message success, userId: {}, count: {}", userId, messageIds.size());
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量删除消息成功");
            response.setSuccess(true);
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
            
            // 统计个人消息未读数量
            Long personalCount = messageMapper.countUnreadMessages(userId);
            
            // 统计群发消息未读数量
            Long broadcastCount = messageRecipientMapper.countUnreadByReceiverId(userId);
            
            Long totalCount = (personalCount != null ? personalCount : 0L) + 
                             (broadcastCount != null ? broadcastCount : 0L);
            
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
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
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
            
            // 创建群发消息
            Message message = new Message();
            message.setMessageType(request.getMessageType() != null ? 
                    request.getMessageType() : MessageType.BROADCAST.getCode());
            message.setSenderId(0L);  // 系统推送消息,senderId为0
            message.setSenderName("系统");
            message.setReceiverId(null);  // 群发消息receiverId为null
            message.setTitle(request.getTitle());
            message.setContent(request.getContent());
            message.setStatus(MessageStatus.UNREAD.getCode());
            message.setIsBroadcast(1);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());
            
            // 插入消息
            int rows = messageMapper.insert(message);
            if (rows <= 0) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("创建批量推送消息失败");
                return response;
            }
            
            // 创建接收者记录
            List<MessageRecipient> recipients = request.getReceiverIds().stream()
                    .map(receiverId -> {
                        MessageRecipient recipient = new MessageRecipient();
                        recipient.setMessageId(message.getMessageId());
                        recipient.setReceiverId(receiverId);
                        recipient.setStatus(MessageStatus.UNREAD.getCode());
                        recipient.setCreateTime(new Date());
                        recipient.setUpdateTime(new Date());
                        return recipient;
                    })
                    .collect(Collectors.toList());
            
            // 批量插入接收者
            int recipientRows = messageRecipientMapper.batchInsert(recipients);
            log.info("Batch push message success, messageId: {}, recipients count: {}", 
                    message.getMessageId(), recipientRows);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("批量推送消息成功");
            response.setMessageId(message.getMessageId());
            response.setSuccessCount(recipientRows);
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
