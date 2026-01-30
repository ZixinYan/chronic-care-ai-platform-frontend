package com.zixin.healthcenterapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取报告详情请求
 * 
 * @author zixin
 */
@Data
public class GetReportDetailRequest {
    /**
     * 报告ID
     */
    private Long reportId;
}
