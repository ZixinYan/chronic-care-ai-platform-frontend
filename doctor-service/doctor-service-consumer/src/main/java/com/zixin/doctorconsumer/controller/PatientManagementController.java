package com.zixin.doctorconsumer.controller;

import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.api.PatientManagementAPI;
import com.zixin.doctorapi.dto.MyPatientsRequest;
import com.zixin.doctorapi.dto.MyPatientsResponse;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor/patient")
@Slf4j
public class PatientManagementController {
    @DubboReference(check = false)
    private PatientManagementAPI patientManagementAPI;

    @PostMapping("/patients")
    public Result<?> getPatients(@RequestBody MyPatientsRequest request) {
        Long doctorId = UserInfoManager.getUserId();
        request.setDoctorId(doctorId);
        MyPatientsResponse response = patientManagementAPI.myPatients(request);
        if(response.getCode().equals(ToBCodeEnum.SUCCESS)) {
            return Result.success(response.getPatients());
        } else {
            return Result.error(response.getMessage());
        }
    }
}
