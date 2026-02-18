package com.zixin.messageprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.messageapi.po.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 查询用户收到的消息列表
     *
     * @param receiverId 接收者ID
     * @param messageType 消息类型
     * @param status 消息状态
     * @return 消息列表
     */
    List<Message> selectInboxMessages(@Param("receiverId") Long receiverId,
                                       @Param("messageType") Integer messageType,
                                       @Param("status") Integer status);
    
    /**
     * 查询用户发送的消息列表
     *
     * @param senderId 发送者ID
     * @param messageType 消息类型
     * @param status 消息状态
     * @return 消息列表
     */
    List<Message> selectSentMessages(@Param("senderId") Long senderId,
                                      @Param("messageType") Integer messageType,
                                      @Param("status") Integer status);
    
    /**
     * 统计用户未读消息数量
     *
     * @param receiverId 接收者ID
     * @return 未读消息数量
     */
    Long countUnreadMessages(@Param("receiverId") Long receiverId);
}
