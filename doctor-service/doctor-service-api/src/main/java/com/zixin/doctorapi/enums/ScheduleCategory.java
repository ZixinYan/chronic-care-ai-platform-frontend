package com.zixin.doctorapi.enums;

import lombok.Getter;

/**
 * 排班分类枚举
 *
 * @author zixin
 */
@Getter
public enum ScheduleCategory {

    OUTPATIENT(1, "门诊", "OUTPATIENT", "门诊接诊患者"),
    SURGERY(2, "手术", "SURGERY", "手术安排"),
    WARD_ROUND(3, "查房", "WARD_ROUND", "病房巡查"),
    CONSULTATION(4, "会诊", "CONSULTATION", "多科室会诊"),
    EMERGENCY(5, "急诊", "EMERGENCY", "急诊处理"),
    TEACHING(6, "教学", "TEACHING", "教学培训"),
    RESEARCH(7, "科研", "RESEARCH", "科研工作"),
    ADMINISTRATION(8, "行政", "ADMINISTRATION", "行政事务"),
    ONLINE_APPROVAL(9, "线上审批", "ONLINE_APPROVAL", "线上审批");

    /**
     * 分类编码
     */
    private final Integer code;

    /**
     * 分类名称
     */
    private final String name;

    /**
     * 分类标识
     */
    private final String type;

    /**
     * 分类描述
     */
    private final String description;

    ScheduleCategory(Integer code, String name, String type, String description) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ScheduleCategory getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScheduleCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 根据type获取枚举
     */
    public static ScheduleCategory getByType(String type) {
        if (type == null) {
            return null;
        }
        for (ScheduleCategory category : values()) {
            if (category.getType().equals(type)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 根据name获取枚举
     */
    public static ScheduleCategory getByName(String name) {
        if (name == null) {
            return null;
        }
        for (ScheduleCategory category : values()) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 判断code是否有效
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取分类名称
     */
    public static String getNameByCode(Integer code) {
        ScheduleCategory category = getByCode(code);
        return category != null ? category.getName() : null;
    }

    /**
     * 获取分类描述
     */
    public static String getDescriptionByCode(Integer code) {
        ScheduleCategory category = getByCode(code);
        return category != null ? category.getDescription() : null;
    }

    @Override
    public String toString() {
        return this.code + "-" + this.name;
    }
}