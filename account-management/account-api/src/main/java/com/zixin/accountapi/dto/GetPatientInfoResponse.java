package com.zixin.accountapi.dto;

import com.zixin.accountapi.vo.PatientVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取患者信息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetPatientInfoResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 患者信息
     */
    private PatientVO patient;
}
