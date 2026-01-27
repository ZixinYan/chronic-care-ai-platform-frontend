package com.zixin.doctorapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日程优先级枚举
 */
@Getter
@AllArgsConstructor
public enum SchedulePriority {
    
    /**
     * 低优先级
     */
    LOW(1, "低"),
    
    /**
     * 中等优先级
     */
    MEDIUM(2, "中"),
    
    /**
     * 高优先级
     */
    HIGH(3, "高"),
    
    /**
     * 紧急
     */
    URGENT(4, "紧急");
    
    private final Integer code;
    private final String description;
    
    public static SchedulePriority fromCode(Integer code) {
        for (SchedulePriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + code);
    }
}
