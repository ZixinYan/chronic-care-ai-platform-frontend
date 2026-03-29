package com.zixin.healthcenterapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取报告详情请求
 * 
 * @author zixin
 */
@Data
public class GetReportDetailRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 报告ID
     */
    private Long reportId;
}
