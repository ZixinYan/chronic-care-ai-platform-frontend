package com.zixin.doctorapi.api;

import com.zixin.doctorapi.dto.MyPatientsRequest;
import com.zixin.doctorapi.dto.MyPatientsResponse;

public interface PatientManagementAPI {

    /**
     * 查询医生的患者列表
     */
    MyPatientsResponse myPatients(MyPatientsRequest request);
}
