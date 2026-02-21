package com.zixin.accountapi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医生信息VO
 * 
 * 整合Account和Doctor的信息
 */
@Data
public class DoctorVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 医生ID
     */
    private Long id;
    
    /**
     * 账户ID
     */
    private Long userId;
    
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
