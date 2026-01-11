package com.zixin.accountprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.AccountRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountRoleMapper extends BaseMapper<AccountRole> {
    /**
     * 根据 userId 查询角色 code 列表
     */
    List<Integer> selectRoleCodesByUserId(@Param("userId") Long userId);
}
