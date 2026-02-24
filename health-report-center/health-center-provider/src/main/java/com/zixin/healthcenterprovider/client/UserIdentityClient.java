package com.zixin.healthcenterprovider.client;

import com.zixin.accountapi.api.UserIdentityAPI;
import com.zixin.accountapi.dto.GetDoctorInfoRequest;
import com.zixin.accountapi.dto.GetDoctorInfoResponse;
import com.zixin.accountapi.dto.GetPatientInfoRequest;
import com.zixin.accountapi.dto.GetPatientInfoResponse;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserIdentityClient {
    @DubboReference(check = false)
    private UserIdentityAPI userIdentityAPI;

    public PatientVO getPatientInfo(Long userId) {
        GetPatientInfoResponse response = userIdentityAPI.getPatientInfo(GetPatientInfoRequest.builder()
                .userId(userId)
                .build());
        
        if (!response.getCode().equals(ToBCodeEnum.SUCCESS)) {
            log.error("Failed to get patient info for userId: {}, message: {}", userId, response.getMessage());
            return null;
        }
        
        return response.getPatient();
    }

    public DoctorVO getDoctorInfo(Long userId) {
        GetDoctorInfoResponse response = userIdentityAPI.getDoctorInfo(GetDoctorInfoRequest.builder()
                .userId(userId)
                .build());
        
        if (!response.getCode().equals(ToBCodeEnum.SUCCESS)) {
            log.error("Failed to get doctor info for userId: {}, message: {}", userId, response.getMessage());
            return null;
        }
        
        return response.getDoctor();
    }
}

