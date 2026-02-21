package com.zixin.accountprovider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zixin.accountapi.api.UserIdentityAPI;
import com.zixin.accountapi.dto.*;
import com.zixin.accountapi.po.User;
import com.zixin.accountapi.po.Doctor;
import com.zixin.accountapi.po.Patient;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.accountprovider.mapper.UserMapper;
import com.zixin.accountprovider.mapper.DoctorMapper;
import com.zixin.accountprovider.mapper.PatientMapper;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户身份信息服务实现
 * 
 * 提供医生、患者等不同身份用户的信息查询服务
 * 整合Account基础信息和身份扩展信息
 */
@Service
@DubboService
@Slf4j
public class UserIdentityServiceImpl implements UserIdentityAPI {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private PatientMapper patientMapper;
    
    @Override
    public GetDoctorInfoResponse getDoctorInfo(GetDoctorInfoRequest request) {
        GetDoctorInfoResponse response = new GetDoctorInfoResponse();
        
        try {
            // 查询医生信息
            Doctor doctor = null;
            if (request.getDoctorId() != null) {
                doctor = doctorMapper.selectById(request.getDoctorId());
            } else if (request.getUserId() != null) {
                LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Doctor::getUserId, request.getUserId());
                doctor = doctorMapper.selectOne(wrapper);
            }
            
            if (doctor == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("医生不存在");
                return response;
            }
            
            // 查询账户信息
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("关联账户不存在");
                return response;
            }
            
            // 组装VO
            DoctorVO vo = new DoctorVO();
            BeanUtils.copyProperties(doctor, vo);
            BeanUtils.copyProperties(user, vo);
            vo.setUsername(user.getUsername());

            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setDoctor(vo);
            
            log.info("Get doctor info success, doctorId: {}, userId: {}", 
                    doctor.getId(), doctor.getUserId());
        } catch (Exception e) {
            log.error("Get doctor info error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询医生信息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetDoctorInfoResponse getDoctorInfoByUserId(Long userId) {
        return getDoctorInfo(GetDoctorInfoRequest
                .builder()
                .userId(userId)
                .build());
    }
    
    @Override
    public GetPatientInfoResponse getPatientInfo(GetPatientInfoRequest request) {
        GetPatientInfoResponse response = new GetPatientInfoResponse();
        
        try {
            // 查询患者信息
            Patient patient = null;
            if (request.getPatientId() != null) {
                patient = patientMapper.selectById(request.getPatientId());
            } else if (request.getUserId() != null) {
                LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Patient::getUserId, request.getUserId());
                patient = patientMapper.selectOne(wrapper);
            }
            
            if (patient == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("患者不存在");
                return response;
            }
            
            // 查询账户信息
            User user = userMapper.selectById(patient.getUserId());
            if (user == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("关联账户不存在");
                return response;
            }
            
            // 组装VO
            PatientVO vo = new PatientVO();
            BeanUtils.copyProperties(patient, vo);
            BeanUtils.copyProperties(user, vo);
            vo.setUsername(user.getUsername());
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setPatient(vo);
            
            log.info("Get patient info success, patientId: {}, userId: {}", 
                    patient.getId(), patient.getUserId());
        } catch (Exception e) {
            log.error("Get patient info error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询患者信息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetPatientInfoResponse getPatientInfoByUserId(Long userId) {
        return getPatientInfo(GetPatientInfoRequest
                .builder()
                .userId(userId)
                .build());
    }
}
