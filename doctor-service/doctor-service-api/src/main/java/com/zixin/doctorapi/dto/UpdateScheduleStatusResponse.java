package com.zixin.doctorapi.dto;

import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新日程状态响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateScheduleStatusResponse extends BaseResponse {
    
    /**
     * 更新的日程ID
     */
    private Long scheduleId;
    
    /**
     * 新状态
     */
    private String status;
}
