package com.zixin.doctorapi.dto;

import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 更新日程状态响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateScheduleStatusResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 更新的日程ID
     */
    private Long scheduleId;
    
    /**
     * 新状态
     */
    private String status;
}
