package com.zixin.healthcenterapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProcessReportRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private Long reportId;

    /**
     * 处理结果 (1-同意, 2-拒绝)
     */
    private Integer result;

    /**
     * 处理意见
     */
    private String comment;
}
