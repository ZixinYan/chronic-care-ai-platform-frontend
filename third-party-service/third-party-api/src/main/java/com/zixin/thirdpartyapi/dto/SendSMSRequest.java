package com.zixin.thirdpartyapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendSMSRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String phone;
    private String code;
    private String templateId;
}
