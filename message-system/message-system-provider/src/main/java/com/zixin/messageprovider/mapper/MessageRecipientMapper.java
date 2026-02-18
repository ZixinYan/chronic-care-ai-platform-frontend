package com.zixin.messageprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.messageapi.po.MessageRecipient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息接收者Mapper
 */
@Mapper
public interface MessageRecipientMapper extends BaseMapper<MessageRecipient> {
    
    /**
     * 查询用户收到的群发消息列表
     *
     * @param receiverId 接收者ID
     * @param status 消息状态
     * @return 消息接收者记录列表
     */
    List<MessageRecipient> selectByReceiverId(@Param("receiverId") Long receiverId,
                                               @Param("status") Integer status);
    
    /**
     * 查询消息的所有接收者
     *
     * @param messageId 消息ID
     * @return 消息接收者记录列表
     */
    List<MessageRecipient> selectByMessageId(@Param("messageId") Long messageId);
    
    /**
     * 批量插入接收者
     *
     * @param recipients 接收者列表
     * @return 插入数量
     */
    int batchInsert(@Param("recipients") List<MessageRecipient> recipients);
    
    /**
     * 统计用户未读的群发消息数量
     *
     * @param receiverId 接收者ID
     * @return 未读消息数量
     */
    Long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
}
