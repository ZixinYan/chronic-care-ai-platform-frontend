package com.zixin.accountapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String address;
    private Integer gender;
    private String idCard;
    private Date birthday;
    
    /**
     * 用户角色列表 (可选)
     * 如果不传，使用系统默认角色
     */
    private List<Integer> roleCodes;
}
