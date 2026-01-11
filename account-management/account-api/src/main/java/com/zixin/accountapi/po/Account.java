package com.zixin.accountapi.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("`care_platform_user`")
public class Account {
    @TableId(type = IdType.ASSIGN_ID)
    private Long accountId;
    private String username;
    private String nickname;
    private Integer gender;
    private String password;
    private String phone;
    private String email;
    private String avatarUrl;
    private String address;
    private Date birthday;
    private String idCard;
    private Date createTime;
    private Date updateTime;
    @TableLogic
    private Integer dele;
    @Version
    private Integer version;
    private JSON ext;
}
