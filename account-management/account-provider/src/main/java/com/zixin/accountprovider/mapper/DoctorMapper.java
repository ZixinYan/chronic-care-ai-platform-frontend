package com.zixin.accountprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生信息Mapper
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
