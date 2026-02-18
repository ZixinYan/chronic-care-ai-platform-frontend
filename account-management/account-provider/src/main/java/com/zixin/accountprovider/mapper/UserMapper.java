package com.zixin.accountprovider.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
