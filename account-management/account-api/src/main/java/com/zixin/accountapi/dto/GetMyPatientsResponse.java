package com.zixin.accountapi.dto;

import com.zixin.accountapi.vo.PatientVO;
import com.zixin.utils.utils.BaseResponse;
import com.zixin.utils.utils.PageUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetMyPatientsResponse extends BaseResponse implements Serializable {
    private final static long serialVersionUID = 1L;
    private PageUtils patients;
}
