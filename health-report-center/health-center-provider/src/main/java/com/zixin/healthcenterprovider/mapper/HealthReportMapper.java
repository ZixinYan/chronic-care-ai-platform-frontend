package com.zixin.healthcenterprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.healthcenterapi.po.HealthReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康报告Mapper
 * 
 * @author zixin
 */
@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
}
