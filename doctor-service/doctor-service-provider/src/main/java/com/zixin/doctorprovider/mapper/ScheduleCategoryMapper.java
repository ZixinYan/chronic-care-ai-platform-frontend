package com.zixin.doctorprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.doctorapi.po.ScheduleCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日程类别Mapper
 */
@Mapper
public interface ScheduleCategoryMapper extends BaseMapper<ScheduleCategory> {
}
