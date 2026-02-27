package com.zixin.doctorapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 完成日程请求
 */
@Data
public class CompleteScheduleRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 日程ID
     */
    private Long scheduleId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 诊断报告
     */
    private String diagnosisReport;
    
    /**
     * 处方信息 (可选)
     */
    private String prescription;
    
    /**
     * 备注
     */
    private String notes;
}
