package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取未读数量响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnreadCountResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 未读消息数量
     */
    private Long unreadCount;
}
