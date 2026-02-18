package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取医生信息请求
 */
@Data
public class GetDoctorInfoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 账户ID
     */
    private Long accountId;
}
