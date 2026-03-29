package com.zixin.doctorapi.dto;

import com.zixin.doctorapi.vo.ScheduleVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddScheduleRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private ScheduleVO schedule;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
}
