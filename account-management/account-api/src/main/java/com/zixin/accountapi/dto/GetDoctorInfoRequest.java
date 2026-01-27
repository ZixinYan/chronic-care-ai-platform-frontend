package com.zixin.accountapi.dto;

import lombok.Data;

/**
 * 获取医生信息请求
 */
@Data
public class GetDoctorInfoRequest {
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 账户ID
     */
    private Long accountId;
}
