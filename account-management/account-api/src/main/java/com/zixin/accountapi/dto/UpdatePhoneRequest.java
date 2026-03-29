package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePhoneRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private String newPhone;
}



