package com.zixin.accountapi.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("care_platform_user_role")
public class AccountRole {
    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long accountRoleId;
    /** 用户 ID */
    private Long userId;
    /** RoleCode.code */
    private Integer roleCode;
    private Date createTime;
}
