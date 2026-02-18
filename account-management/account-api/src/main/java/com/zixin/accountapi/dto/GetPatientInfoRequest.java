package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取患者信息请求
 */
@Data
public class GetPatientInfoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 账户ID
     */
    private Long accountId;
}
