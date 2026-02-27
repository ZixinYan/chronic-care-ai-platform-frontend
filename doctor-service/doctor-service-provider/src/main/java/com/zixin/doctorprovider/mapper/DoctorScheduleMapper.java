package com.zixin.doctorprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.doctorapi.po.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /**
     * 批量插入
     */
    int batchInsert(@Param("list") List<DoctorSchedule> list);
    /**
     * 批量插入或更新（主键冲突时更新）
     */
    int batchInsertOrUpdate(@Param("list") List<DoctorSchedule> list);
    /**
     * 批量插入（指定批次大小）
     */
    int batchInsertWithBatchSize(@Param("list") List<DoctorSchedule> list);
}