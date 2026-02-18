package com.zixin.doctorapi.dto;

import com.zixin.doctorapi.vo.DoctorVO;
import com.zixin.utils.utils.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取医生信息响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetDoctorInfoResponse extends BaseResponse {
    
    /**
     * 医生信息
     */
    private DoctorVO doctor;
}
