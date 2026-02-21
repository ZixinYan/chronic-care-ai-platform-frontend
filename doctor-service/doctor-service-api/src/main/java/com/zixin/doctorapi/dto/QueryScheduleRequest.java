package com.zixin.doctorapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询日程请求
 */
@Data
public class QueryScheduleRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 日期 (可选，格式: YYYY-MM-DD)
     */
    private String scheduleDay;
    
    /**
     * 状态 (可选)
     */
    private String status;
    
    /**
     * 日程类别 (可选)
     */
    private Integer scheduleCategory;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
