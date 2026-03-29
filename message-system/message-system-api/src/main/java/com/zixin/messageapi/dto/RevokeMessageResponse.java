package com.zixin.messageapi.dto;

import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 撤回消息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RevokeMessageResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 是否成功
     */
    private Boolean success;
}
