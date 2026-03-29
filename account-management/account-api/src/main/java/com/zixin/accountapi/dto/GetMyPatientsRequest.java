package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetMyPatientsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long doctorId;
    private String patientName; // 用于模糊匹配
    private int pageNum;
    private int pageSize;
}
