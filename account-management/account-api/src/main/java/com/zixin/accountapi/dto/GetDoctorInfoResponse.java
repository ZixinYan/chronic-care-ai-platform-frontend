package com.zixin.accountapi.dto;

import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.utils.utils.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取医生信息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetDoctorInfoResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 医生信息
     */
    private DoctorVO doctor;
}
