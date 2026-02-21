package com.zixin.doctorprovider.client;

import com.zixin.accountapi.api.AccountManagementAPI;
import com.zixin.accountapi.api.UserIdentityAPI;
import com.zixin.accountapi.dto.*;
import com.zixin.accountapi.po.Doctor;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.utils.exception.ToBCodeEnum;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class DoctorClient {
    @DubboReference(check = false)
    private UserIdentityAPI userIdentityAPI;

    public DoctorVO getDoctorInfo(GetDoctorInfoRequest request) {
        GetDoctorInfoResponse doctorInfoResponse = userIdentityAPI.getDoctorInfo(request);
        if (!doctorInfoResponse.getCode().equals(ToBCodeEnum.SUCCESS)) {
            throw new RuntimeException("Failed to get doctor info: " + doctorInfoResponse.getMessage());
        }
        return doctorInfoResponse.getDoctor();
    }

    public PatientVO getPatientInfo(GetPatientInfoRequest request) {
        GetPatientInfoResponse patientInfoResponse = userIdentityAPI.getPatientInfo(request);
        if (!patientInfoResponse.getCode().equals(ToBCodeEnum.SUCCESS)) {
            throw new RuntimeException("Failed to get patient info: " + patientInfoResponse.getMessage());
        }
        return patientInfoResponse.getPatient();
    }
}
