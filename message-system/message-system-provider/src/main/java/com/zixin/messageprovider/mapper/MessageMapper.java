package com.zixin.messageprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zixin.messageapi.po.Message;
import com.zixin.messageapi.vo.MessageVO;
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
     * @param userId 接收者ID
     * @param messageType 消息类型
     * @param status 消息状态
     * @return 消息列表
     */
    @Deprecated
    IPage<Message> selectInboxMessages(Page<?> page,
                                         @Param("userId") Long userId,
                                         @Param("messageType") Integer messageType,
                                         @Param("status") Integer status);
    /**
     * 查询用户发送的消息列表
     *
     * @param userId 发送者ID
     * @param messageType 消息类型
     * @param status 消息状态
     * @return 消息列表
     */
    @Deprecated
    IPage<Message> selectSentMessages(Page<?> page,
                                      @Param("userId") Long userId,
                                      @Param("messageType") Integer messageType,
                                      @Param("status") Integer status);
    /**
     * 统计用户未读消息数量
     *
     * @param receiverId 接收者ID
     * @return 未读消息数量
     */
    Long countUnreadMessages(@Param("receiverId") Long receiverId);
    
    /**
     * 批量查询消息
     *
     * @param messageIds 消息ID列表
     * @return 消息列表
     */
    List<Message> batchSelectByIds(@Param("messageIds") List<Long> messageIds);
    
    /**
     * 批量更新消息状态
     *
     * @param messageIds 消息ID列表
     * @param userId 用户ID（用于权限验证）
     * @param status 新状态
     * @param readTime 阅读时间（可为null，Unix毫秒时间戳）
     * @param updateTime 更新时间（Unix毫秒时间戳）
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("messageIds") List<Long> messageIds,
                          @Param("userId") Long userId,
                          @Param("status") Integer status,
                          @Param("readTime") Long readTime,
                          @Param("updateTime") Long updateTime);
    
    /**
     * 批量删除消息（软删除）
     *
     * @param messageIds 消息ID列表
     * @param userId 用户ID（用于权限验证）
     * @param updateTime 更新时间（Unix毫秒时间戳）
     * @return 删除数量
     */
    int batchDeleteByIds(@Param("messageIds") List<Long> messageIds,
                         @Param("userId") Long userId,
                         @Param("updateTime") Long updateTime);
}
