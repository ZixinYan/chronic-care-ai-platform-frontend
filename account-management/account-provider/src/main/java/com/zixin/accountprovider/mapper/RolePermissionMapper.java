package com.zixin.accountprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    /**
     * 根据 roleCode 列表查询权限
     */
    List<RolePermission> selectByRoleCodes(@Param("roleCodes") List<Integer> roleCodes);
}
