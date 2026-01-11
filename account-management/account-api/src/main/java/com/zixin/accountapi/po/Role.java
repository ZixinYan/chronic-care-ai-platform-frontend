package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("care_platform_role")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long roleId;

    private Integer code;   // DOCTOR / PATIENT / FAMILY
    private String name;
    private String description;

    /** 业务可配置权限 */
    private Integer action;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    private Date createTime;
    private Date updateTime;
}
