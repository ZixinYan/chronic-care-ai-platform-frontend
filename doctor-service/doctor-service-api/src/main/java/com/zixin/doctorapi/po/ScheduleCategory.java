package com.zixin.doctorapi.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 日程类别表
 * 
 * 定义各种日程类型
 */
@Data
@TableName("doctor_schedule_category")
public class ScheduleCategory {
    
    /**
     * 类别ID (主键)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 类别名称 (如: 门诊、手术、查房、会诊)
     */
    private String categoryName;
    
    /**
     * 类别别名/简称
     */
    private String categoryAlias;
    
    /**
     * 类别描述
     */
    private String describe;
    
    /**
     * 扩展字段 (JSON格式)
     * 可以存储: 颜色标识、图标、是否需要AI推荐等
     */
    private JSON ext;
}
