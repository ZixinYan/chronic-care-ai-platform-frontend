package com.zixin.doctorapi.dto;

import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取日程详情响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetScheduleDetailResponse extends BaseResponse {
    
    /**
     * 日程详情
     */
    private ScheduleVO schedule;
}
