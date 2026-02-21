package com.zixin.doctorapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.utils.BaseResponse;

/**
 * AI生成日程建议响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenerateScheduleResponse extends BaseResponse implements Serializable {
    private final static long serialVersionUID = 1L;
    
    /**
     * AI推荐的日程列表
     */
    private List<ScheduleVO> recommendedSchedules;
    
    /**
     * AI推荐理由
     */
    private String recommendation;
}
