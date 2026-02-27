package com.zixin.doctorapi.dto;

import com.zixin.accountapi.vo.PatientVO;
import com.zixin.utils.utils.BaseResponse;
import com.zixin.utils.utils.PageUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MyPatientsResponse extends BaseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private PageUtils patients;
}
