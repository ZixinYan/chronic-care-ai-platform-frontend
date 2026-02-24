package com.zixin.doctorapi.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

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
     * 医生姓名(redundant, 方便查询展示，实际使用中可以通过doctorId关联查询获取)
     */
    private String doctorName;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名(redundant, 方便查询展示，实际使用中可以通过patientId关联查询获取)
     */
    private String patientName;

    /**
     * 日程内容/描述
     */
    private String schedule;
    
    /**
     * 日程类别ID (关联schedule_category表)
     */
    private Long scheduleCategory;
    
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
     * 日程开始时间
     */
    private Long startTime;

    /**
     * 日程结束时间
     */
    private Long endTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
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
