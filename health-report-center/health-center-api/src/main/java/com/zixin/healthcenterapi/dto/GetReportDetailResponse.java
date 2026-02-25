package com.zixin.healthcenterapi.dto;

import com.zixin.healthcenterapi.vo.HealthReportVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取报告详情响应
 * 
 * @author zixin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetReportDetailResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 报告详情
     */
    private HealthReportVO report;
}
