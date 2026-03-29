package com.zixin.accountprovider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zixin.accountapi.po.Patient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者信息Mapper
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
