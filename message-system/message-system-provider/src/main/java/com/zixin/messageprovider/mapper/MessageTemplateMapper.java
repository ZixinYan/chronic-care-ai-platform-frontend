package com.zixin.messageprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.messageapi.po.MessageTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息模板Mapper
 * 
 * @author zixin
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {
    
}
