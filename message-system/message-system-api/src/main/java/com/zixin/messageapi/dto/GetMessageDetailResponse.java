package com.zixin.messageapi.dto;

import com.zixin.messageapi.vo.MessageVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取消息详情响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetMessageDetailResponse extends BaseResponse {
    
    /**
     * 消息详情
     */
    private MessageVO messageVO;
}
