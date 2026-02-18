package com.zixin.doctorapi.dto;

import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 完成日程响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CompleteScheduleResponse extends BaseResponse {
    
    /**
     * 更新后的日程信息
     */
    private ScheduleVO schedule;
}
