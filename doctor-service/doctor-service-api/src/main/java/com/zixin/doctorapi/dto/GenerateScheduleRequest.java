package com.zixin.doctorapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI生成日程建议请求
 */
@Data
public class GenerateScheduleRequest implements Serializable {
    private final static long serialVersionUID = 1L;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 日期 (格式: YYYY-MM-DD)
     */
    private String scheduleDay;
    
    /**
     * 科室 (可选，用于AI推荐)
     */
    private String department;
}
