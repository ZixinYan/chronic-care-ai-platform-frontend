package com.zixin.doctorprovider.service;

import com.zixin.accountapi.dto.GetMyPatientsRequest;
import com.zixin.doctorapi.api.PatientManagementAPI;
import com.zixin.doctorapi.dto.MyPatientsRequest;
import com.zixin.doctorapi.dto.MyPatientsResponse;
import com.zixin.doctorprovider.client.DoctorClient;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Service
@DubboService
@Slf4j
public class PatientManagementServiceImpl implements PatientManagementAPI {
    private final DoctorClient doctorClient;

    public PatientManagementServiceImpl(DoctorClient doctorClient) {
        this.doctorClient = doctorClient;
    }

    @Override
    public MyPatientsResponse myPatients(MyPatientsRequest request) {
        // 2. 查询患者表，获取attendingDoctorId包含当前医生ID的患者列表
        GetMyPatientsRequest getMyPatientsRequest = new GetMyPatientsRequest();
        getMyPatientsRequest.setDoctorId(request.getDoctorId());
        getMyPatientsRequest.setPageNum(request.getPageNum() > 0 ? request.getPageNum() : 1);
        getMyPatientsRequest.setPageSize(request.getPageSize() > 0 ? request.getPageSize() : 20);
        if(request.getPatientName() != null) {
            log.info("Query my patients with patientId filter, doctorId: {}, patientName: {}", request.getDoctorId(), request.getPatientName());
            getMyPatientsRequest.setPatientName(request.getPatientName());
        }
        PageUtils patients = doctorClient.getPatientsByDoctorId(getMyPatientsRequest);
        // 3. 构建响应
        MyPatientsResponse response = new MyPatientsResponse();
        response.setCode(ToBCodeEnum.SUCCESS);
        response.setMessage("查询患者列表成功");
        response.setPatients(patients);
        log.info("Query my patients success, doctorId: {}, patientCount: {}", request.getDoctorId(), patients.getTotalCount());
        return response;
    }
}
