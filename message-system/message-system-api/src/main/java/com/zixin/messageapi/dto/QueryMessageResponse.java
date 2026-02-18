package com.zixin.messageapi.dto;

import com.zixin.messageapi.vo.MessageVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询消息列表响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryMessageResponse extends BaseResponse {
    
    /**
     * 消息列表
     */
    private List<MessageVO> messages;
    
    /**
     * 总数量
     */
    private Long total;
}
