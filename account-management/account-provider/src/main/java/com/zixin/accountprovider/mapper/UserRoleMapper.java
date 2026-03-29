package com.zixin.accountprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    List<Integer> selectRoleCodesByUserId(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);

    void batchInsert(@Param("list") List<UserRole> userRoles);
}