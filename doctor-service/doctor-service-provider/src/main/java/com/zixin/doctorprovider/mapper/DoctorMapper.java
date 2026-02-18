package com.zixin.doctorprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.doctorapi.po.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生信息Mapper
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
