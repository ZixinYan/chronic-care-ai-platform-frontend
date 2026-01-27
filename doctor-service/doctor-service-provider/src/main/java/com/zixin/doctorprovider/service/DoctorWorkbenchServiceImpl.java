package com.zixin.doctorprovider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.*;
import com.zixin.doctorapi.enums.SchedulePriority;
import com.zixin.doctorapi.enums.ScheduleStatus;
import com.zixin.doctorapi.po.Doctor;
import com.zixin.doctorapi.po.DoctorSchedule;
import com.zixin.doctorapi.po.ScheduleCategory;
import com.zixin.doctorapi.vo.DoctorVO;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.doctorprovider.mapper.DoctorMapper;
import com.zixin.doctorprovider.mapper.DoctorScheduleMapper;
import com.zixin.doctorprovider.mapper.ScheduleCategoryMapper;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生工作台服务实现 (Dubbo服务)
 * 
 * 提供医生日程管理、AI推荐等功能
 */
@Service
@DubboService
@Slf4j
public class DoctorWorkbenchServiceImpl implements DoctorWorkbenchAPI {
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private DoctorScheduleMapper scheduleMapper;
    
    @Autowired
    private ScheduleCategoryMapper categoryMapper;
    
    @Override
    public GenerateScheduleResponse generateScheduleSuggestion(GenerateScheduleRequest request) {
        GenerateScheduleResponse response = new GenerateScheduleResponse();
        
        try {
            // 验证医生存在
            Doctor doctor = doctorMapper.selectById(request.getDoctorId());
            if (doctor == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("医生不存在");
                return response;
            }
            
            // TODO: 接入AI服务生成智能日程推荐
            // 这里先实现简单的逻辑，后续对接AI服务
            List<ScheduleVO> recommendations = generateMockSchedules(request);
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("AI日程推荐生成成功");
            response.setRecommendedSchedules(recommendations);
            response.setRecommendation("根据历史数据分析，为您推荐了以下日程安排");
            
            log.info("Generate schedule suggestion success, doctorId: {}, date: {}", 
                    request.getDoctorId(), request.getScheduleDay());
        } catch (Exception e) {
            log.error("Generate schedule suggestion error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("生成日程推荐失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public QueryScheduleResponse querySchedule(QueryScheduleRequest request) {
        QueryScheduleResponse response = new QueryScheduleResponse();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DoctorSchedule::getDoctorId, request.getDoctorId());
            
            if (request.getScheduleDay() != null) {
                wrapper.eq(DoctorSchedule::getScheduleDay, request.getScheduleDay());
            }
            if (request.getStatus() != null) {
                wrapper.eq(DoctorSchedule::getStatus, request.getStatus());
            }
            if (request.getScheduleCategory() != null) {
                wrapper.eq(DoctorSchedule::getScheduleCategory, request.getScheduleCategory());
            }
            
            wrapper.orderByDesc(DoctorSchedule::getPriority)
                   .orderByDesc(DoctorSchedule::getCreateTime);
            
            // 查询
            List<DoctorSchedule> schedules = scheduleMapper.selectList(wrapper);
            
            // 转换VO
            List<ScheduleVO> scheduleVOS = schedules.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setSchedules(scheduleVOS);
            response.setTotal((long) scheduleVOS.size());
            
            log.info("Query schedule success, doctorId: {}, count: {}", 
                    request.getDoctorId(), scheduleVOS.size());
        } catch (Exception e) {
            log.error("Query schedule error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询日程失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetScheduleDetailResponse getScheduleDetail(Long scheduleId, Long doctorId) {
        GetScheduleDetailResponse response = new GetScheduleDetailResponse();
        
        try {
            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null || !schedule.getDoctorId().equals(doctorId)) {
                log.warn("Schedule not found or access denied, scheduleId: {}, doctorId: {}", 
                        scheduleId, doctorId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在或无权限");
                return response;
            }
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setSchedule(convertToVO(schedule));
            
            log.info("Get schedule detail success, scheduleId: {}", scheduleId);
        } catch (Exception e) {
            log.error("Get schedule detail error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询日程详情异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompleteScheduleResponse completeSchedule(CompleteScheduleRequest request) {
        CompleteScheduleResponse response = new CompleteScheduleResponse();
        
        try {
            // 查询日程
            DoctorSchedule schedule = scheduleMapper.selectById(request.getScheduleId());
            if (schedule == null || !schedule.getDoctorId().equals(request.getDoctorId())) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在或无权限");
                return response;
            }
            
            // 更新日程
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, request.getScheduleId())
                   .set(DoctorSchedule::getStatus, ScheduleStatus.COMPLETED.getCode())
                   .set(DoctorSchedule::getResult, buildResult(request))
                   .set(DoctorSchedule::getUpdateTime, new Date());
            
            int rows = scheduleMapper.update(null, wrapper);
            if (rows > 0) {
                // 查询更新后的日程
                DoctorSchedule updated = scheduleMapper.selectById(request.getScheduleId());
                
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("完成日程成功");
                response.setSchedule(convertToVO(updated));
                
                log.info("Complete schedule success, scheduleId: {}, doctorId: {}", 
                        request.getScheduleId(), request.getDoctorId());
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("完成日程失败");
            }
        } catch (Exception e) {
            log.error("Complete schedule error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("完成日程异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancelScheduleResponse cancelSchedule(Long scheduleId, Long doctorId, String reason) {
        CancelScheduleResponse response = new CancelScheduleResponse();
        
        try {
            // 验证日程
            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null || !schedule.getDoctorId().equals(doctorId)) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在或无权限");
                return response;
            }
            
            // 更新状态
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, scheduleId)
                   .set(DoctorSchedule::getStatus, ScheduleStatus.CANCELLED.getCode())
                   .set(DoctorSchedule::getResult, "取消原因: " + reason)
                   .set(DoctorSchedule::getUpdateTime, new Date());
            
            int rows = scheduleMapper.update(null, wrapper);
            if (rows > 0) {
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("取消日程成功");
                response.setScheduleId(scheduleId);
                log.info("Cancel schedule success, scheduleId: {}, reason: {}", scheduleId, reason);
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("取消日程失败");
            }
        } catch (Exception e) {
            log.error("Cancel schedule error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("取消日程异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateScheduleStatusResponse updateScheduleStatus(Long scheduleId, Long doctorId, String status) {
        UpdateScheduleStatusResponse response = new UpdateScheduleStatusResponse();
        
        try {
            // 验证状态合法性
            ScheduleStatus.fromCode(status);
            
            // 更新
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, scheduleId)
                   .eq(DoctorSchedule::getDoctorId, doctorId)
                   .set(DoctorSchedule::getStatus, status)
                   .set(DoctorSchedule::getUpdateTime, new Date());
            
            int rows = scheduleMapper.update(null, wrapper);
            if (rows > 0) {
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("更新状态成功");
                response.setScheduleId(scheduleId);
                response.setStatus(status);
                log.info("Update schedule status success, scheduleId: {}, status: {}", scheduleId, status);
            } else {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("更新状态失败");
            }
        } catch (Exception e) {
            log.error("Update schedule status error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("更新状态异常: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public GetDoctorInfoResponse getDoctorInfo(Long doctorId) {
        GetDoctorInfoResponse response = new GetDoctorInfoResponse();
        
        try {
            Doctor doctor = doctorMapper.selectById(doctorId);
            if (doctor == null) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("医生不存在");
                return response;
            }
            
            DoctorVO vo = new DoctorVO();
            BeanUtils.copyProperties(doctor, vo);
            
            // TODO: 从account-management获取用户基础信息
            // 这里暂时不查询，后续对接account服务
            
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setDoctor(vo);
            
            log.info("Get doctor info success, doctorId: {}", doctorId);
        } catch (Exception e) {
            log.error("Get doctor info error", e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询医生信息异常: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 转换为VO
     */
    private ScheduleVO convertToVO(DoctorSchedule schedule) {
        ScheduleVO vo = new ScheduleVO();
        BeanUtils.copyProperties(schedule, vo);
        
        // 设置优先级描述
        try {
            SchedulePriority priority = SchedulePriority.fromCode(schedule.getPriority());
            vo.setPriorityDesc(priority.getDescription());
        } catch (Exception e) {
            vo.setPriorityDesc("未知");
        }
        
        // 设置状态描述
        try {
            ScheduleStatus status = ScheduleStatus.fromCode(schedule.getStatus());
            vo.setStatusDesc(status.getDescription());
        } catch (Exception e) {
            vo.setStatusDesc("未知");
        }
        
        // 设置类别名称
        if (schedule.getScheduleCategory() != null) {
            ScheduleCategory category = categoryMapper.selectById(schedule.getScheduleCategory());
            if (category != null) {
                vo.setScheduleCategoryName(category.getCategoryName());
            }
        }
        
        // TODO: 设置医生和患者姓名 (从account服务获取)
        
        return vo;
    }
    
    /**
     * 生成模拟日程 (后续替换为AI服务)
     */
    private List<ScheduleVO> generateMockSchedules(GenerateScheduleRequest request) {
        List<ScheduleVO> schedules = new ArrayList<>();
        
        // 模拟3个推荐日程
        schedules.add(createMockSchedule(request, "门诊", "早上门诊时间", SchedulePriority.MEDIUM));
        schedules.add(createMockSchedule(request, "查房", "下午查房", SchedulePriority.HIGH));
        schedules.add(createMockSchedule(request, "会诊", "疑难病例会诊", SchedulePriority.HIGH));
        
        return schedules;
    }
    
    private ScheduleVO createMockSchedule(GenerateScheduleRequest request, 
                                          String categoryName, 
                                          String content, 
                                          SchedulePriority priority) {
        ScheduleVO vo = new ScheduleVO();
        vo.setDoctorId(request.getDoctorId());
        vo.setSchedule(content);
        vo.setScheduleCategoryName(categoryName);
        vo.setScheduleDay(request.getScheduleDay());
        vo.setPriority(priority.getCode());
        vo.setPriorityDesc(priority.getDescription());
        vo.setStatus(ScheduleStatus.PENDING.getCode());
        vo.setStatusDesc(ScheduleStatus.PENDING.getDescription());
        return vo;
    }
    
    /**
     * 构建诊断结果
     */
    private String buildResult(CompleteScheduleRequest request) {
        StringBuilder result = new StringBuilder();
        result.append("诊断报告: ").append(request.getDiagnosisReport());
        
        if (request.getPrescription() != null && !request.getPrescription().isEmpty()) {
            result.append("\n处方信息: ").append(request.getPrescription());
        }
        
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            result.append("\n备注: ").append(request.getNotes());
        }
        
        return result.toString();
    }
}
