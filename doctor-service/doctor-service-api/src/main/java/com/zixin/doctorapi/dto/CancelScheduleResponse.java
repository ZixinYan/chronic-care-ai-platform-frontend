package com.zixin.doctorapi.dto;

import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 取消日程响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CancelScheduleResponse extends BaseResponse {
    
    /**
     * 取消的日程ID
     */
    private Long scheduleId;
}
