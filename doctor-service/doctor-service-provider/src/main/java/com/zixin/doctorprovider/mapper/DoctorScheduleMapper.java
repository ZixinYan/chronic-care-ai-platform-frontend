package com.zixin.doctorprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.doctorapi.po.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生日程Mapper
 */
@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
}
