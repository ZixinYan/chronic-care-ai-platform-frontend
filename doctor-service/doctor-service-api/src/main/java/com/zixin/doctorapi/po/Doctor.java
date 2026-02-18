package com.zixin.doctorapi.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 医生信息表
 * 
 * 继承Account的基础用户信息，扩展医生专属字段
 */
@Data
@TableName("doctor")
public class Doctor {
    
    /**
     * 医生ID (主键)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户ID (关联account表的accountId)
     */
    private Long userId;
    
    /**
     * 科室
     */
    private String department;
    
    /**
     * 职称 (如主治医师、主任医师等)
     */
    private String title;
    
    /**
     * 工作经验 (年数)
     */
    private Integer experience;
    
    /**
     * 执业证书编号
     */
    private String certificationNumber;
    
    /**
     * 学历
     */
    private String education;
    
    /**
     * 简介
     */
    private String bio;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 逻辑删除标记 (0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer dele;
    
    /**
     * 版本号 (乐观锁)
     */
    @Version
    private Integer version;
    
    /**
     * 扩展字段 (JSON格式)
     * 可以存储: 擅长领域、出诊时间、联系方式等
     */
    private JSON ext;
}
