package com.zixin.doctorapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.utils.BaseResponse;

/**
 * 查询日程响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryScheduleResponse extends BaseResponse {
    
    /**
     * 日程列表
     */
    private List<ScheduleVO> schedules;
    
    /**
     * 总数
     */
    private Long total;
}
