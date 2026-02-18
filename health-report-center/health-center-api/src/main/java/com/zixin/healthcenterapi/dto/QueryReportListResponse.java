package com.zixin.healthcenterapi.dto;

import com.zixin.healthcenterapi.vo.HealthReportVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询健康报告列表响应
 * 
 * @author zixin
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryReportListResponse extends BaseResponse {
    
    /**
     * 报告列表
     */
    private List<HealthReportVO> reportList;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
}
