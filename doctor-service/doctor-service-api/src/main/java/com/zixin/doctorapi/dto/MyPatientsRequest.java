package com.zixin.doctorapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MyPatientsRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long doctorId;
    private String patientName; // 可选参数 用于模糊匹配
    private int pageNum;
    private int pageSize;
}
