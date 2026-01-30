package com.zixin.healthcenterapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报告审核状态枚举
 * 
 * @author zixin
 */
@Getter
@AllArgsConstructor
public enum ReportStatus {
    
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");
    
    private final Integer code;
    private final String description;
    
    /**
     * 根据code获取枚举
     */
    public static ReportStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown report status code: " + code);
    }
}
