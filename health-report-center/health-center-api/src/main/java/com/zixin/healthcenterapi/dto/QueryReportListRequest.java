package com.zixin.healthcenterapi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询健康报告列表请求
 * 
 * @author zixin
 */
@Data
public class QueryReportListRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 报告类型 (可选)
     */
    private Integer reportType;
    
    /**
     * 报告分类 (可选)
     */
    private String category;
    
    /**
     * 审核状态 (可选)
     */
    private Integer status;
    
    /**
     * 页码 (默认1)
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量 (默认10)
     */
    private Integer pageSize = 10;
}
