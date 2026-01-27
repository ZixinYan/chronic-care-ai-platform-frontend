package com.zixin.doctorapi.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 医生日程表
 * 
 * 存储医生的工作日程安排
 */
@Data
@TableName("doctor_schedule")
public class DoctorSchedule {
    
    /**
     * 日程ID (主键)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 日程内容/描述
     */
    private String schedule;
    
    /**
     * 日程类别ID (关联schedule_category表)
     */
    private Integer scheduleCategory;
    
    /**
     * 日程日期 (格式: YYYY-MM-DD)
     */
    private String scheduleDay;
    
    /**
     * 优先级 (1-低, 2-中, 3-高, 4-紧急)
     */
    private Integer priority;
    
    /**
     * 日程状态 (PENDING-待处理, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消)
     */
    private String status;
    
    /**
     * 执行结果/诊断报告
     */
    private String result;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 版本号 (乐观锁)
     */
    @Version
    private Integer version;
    
    /**
     * 扩展字段 (JSON格式)
     * 可以存储: 就诊时间段、备注信息、关联检查项等
     */
    private JSON ext;
}
