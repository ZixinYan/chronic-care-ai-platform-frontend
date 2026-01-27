package com.zixin.accountapi.dto;

import lombok.Data;

/**
 * 获取患者信息请求
 */
@Data
public class GetPatientInfoRequest {
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 账户ID
     */
    private Long accountId;
}
