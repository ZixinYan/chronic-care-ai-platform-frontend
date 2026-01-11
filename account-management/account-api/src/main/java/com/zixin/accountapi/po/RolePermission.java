package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("care_platform_role_permission")
public class RolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** RoleCode.code */
    private Integer roleCode;

    /** Action.code */
    private Integer actionCode;

    private Date createTime;
}
