package com.zixin.doctorapi.vo;

import lombok.Data;

/**
 * 医生信息VO
 */
@Data
public class DoctorVO {
    
    /**
     * 医生ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 姓名 (从Account获取)
     */
    private String name;
    
    /**
     * 科室
     */
    private String department;
    
    /**
     * 职称
     */
    private String title;
    
    /**
     * 工作经验 (年)
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
}
