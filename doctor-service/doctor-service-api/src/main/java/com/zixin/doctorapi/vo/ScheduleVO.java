package com.zixin.doctorapi.vo;

import lombok.Data;

/**
 * 日程VO
 */
@Data
public class ScheduleVO {
    
    /**
     * 日程ID
     */
    private Long id;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 医生姓名
     */
    private String doctorName;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 患者姓名
     */
    private String patientName;
    
    /**
     * 日程内容
     */
    private String schedule;
    
    /**
     * 日程类别ID
     */
    private Integer scheduleCategory;
    
    /**
     * 日程类别名称
     */
    private String scheduleCategoryName;
    
    /**
     * 日程日期
     */
    private String scheduleDay;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 优先级描述
     */
    private String priorityDesc;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 执行结果
     */
    private String result;
}
