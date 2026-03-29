package com.zixin.doctorapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日程状态枚举
 */
@Getter
@AllArgsConstructor
public enum ScheduleStatus {
    
    /**
     * 待处理 - 日程已创建但未开始
     */
    PENDING("PENDING", "待处理"),
    
    /**
     * 进行中 - 日程正在执行
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    
    /**
     * 已完成 - 日程已完成并上传报告
     */
    COMPLETED("COMPLETED", "已完成"),
    
    /**
     * 已取消 - 日程被取消
     */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String description;
    
    public static ScheduleStatus fromCode(String code) {
        for (ScheduleStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown schedule status: " + code);
    }
}
