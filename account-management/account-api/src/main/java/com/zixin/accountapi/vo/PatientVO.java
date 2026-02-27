package com.zixin.accountapi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者信息VO
 * 
 * 整合Account和Patient的信息
 */
@Data
public class PatientVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 患者ID
     */
    private Long id;
    
    /**
     * 账户ID
     */
    private Long userId;
    
    /**
     * 主治医生ID
     */
    private Long attendingDoctorId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称/姓名
     */
    private String nickname;
    
    /**
     * 性别 (0-未知, 1-男, 2-女)
     */
    private Integer gender;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 出生日期
     */
    private String birthday;
    
    /**
     * 病史摘要
     */
    private String medicalHistory;
    
    /**
     * 过敏史
     */
    private String allergies;
    
    /**
     * 血型
     */
    private String bloodType;
    
    /**
     * 身高 (cm)
     */
    private Integer height;
    
    /**
     * 体重 (kg)
     */
    private Integer weight;
    
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    
    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;
}
