package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetUserInfoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Long> userIds;
}
