package com.zixin.doctorprovider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zixin.accountapi.dto.GetDoctorInfoRequest;
import com.zixin.accountapi.dto.GetPatientInfoRequest;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.accountapi.vo.PatientVO;
import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.*;
import com.zixin.doctorapi.enums.SchedulePriority;
import com.zixin.doctorapi.enums.ScheduleStatus;
import com.zixin.doctorapi.po.DoctorSchedule;
import com.zixin.doctorapi.po.ScheduleCategory;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.doctorprovider.client.DoctorClient;
import com.zixin.doctorprovider.mapper.DoctorScheduleMapper;
import com.zixin.doctorprovider.mapper.ScheduleCategoryMapper;
import com.zixin.utils.context.UserInfoManager;
import com.zixin.utils.exception.ToBCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
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
 * 
 * 权限验证策略:
 * - Consumer层已验证DOCTOR角色和相关权限
 * - Provider层进行业务级权限验证:
 *   1. 数据归属验证(医生只能操作自己的日程)
 *   2. 状态流转验证(日程状态合法性检查)
 *   3. 业务规则验证(如:诊断报告不能为空)
 * 
 * 安全原则:
 * - 所有操作必须验证doctorId与schedule.doctorId一致
 * - 记录所有权限验证失败的日志,便于审计
 * - 使用@Transactional保证数据一致性
 * 
 * @author zixin
 */
@Service
@DubboService
@Slf4j
public class DoctorWorkbenchServiceImpl implements DoctorWorkbenchAPI {
    private final DoctorScheduleMapper scheduleMapper;
    private final ScheduleCategoryMapper categoryMapper;
    private final DoctorClient doctorClient;

    public DoctorWorkbenchServiceImpl(DoctorScheduleMapper scheduleMapper, ScheduleCategoryMapper categoryMapper, DoctorClient doctorClient) {
        this.scheduleMapper = scheduleMapper;
        this.categoryMapper = categoryMapper;
        this.doctorClient = doctorClient;
    }


    @Override
    public GenerateScheduleResponse generateScheduleSuggestion(GenerateScheduleRequest request) {
        GenerateScheduleResponse response = new GenerateScheduleResponse();

        try {
            // 验证医生存在
            DoctorVO doctorVO = doctorClient.getDoctorInfo(GetDoctorInfoRequest.builder()
                    .userId(request.getDoctorId())
                    .build());
            if (doctorVO == null) {
                log.error("Doctor not found for userId: {}", request.getDoctorId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("医生不存在");
                return response;
            }
            
            // TODO: 接入AI服务生成智能日程推荐
            List<DoctorSchedule> recommendations = generateSchedules(request);
            scheduleMapper.batchInsert(recommendations);
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("AI日程推荐生成成功");
            
            log.info("Generate schedule suggestion success, userId: {}, date: {}",
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
            // 1. 查询日程
            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            
            if (schedule == null) {
                log.warn("Schedule not found, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在");
                return response;
            }
            
            // 2. 只能查看自己的日程
            if (!schedule.getDoctorId().equals(doctorId)) {
                log.warn("Permission denied: doctor {} tried to access schedule {} owned by doctor {}", 
                        doctorId, scheduleId, schedule.getDoctorId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权访问该日程");
                return response;
            }
            
            // 3. 返回日程详情
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("查询成功");
            response.setSchedule(convertToVO(schedule));
            
            log.info("Get schedule detail success, scheduleId: {}, doctorId: {}", scheduleId, doctorId);
        } catch (Exception e) {
            log.error("Get schedule detail failed, scheduleId: {}, doctorId: {}", scheduleId, doctorId, e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("查询日程详情失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompleteScheduleResponse completeSchedule(CompleteScheduleRequest request) {
        CompleteScheduleResponse response = new CompleteScheduleResponse();
        
        try {
            // 1. 查询日程
            DoctorSchedule schedule = scheduleMapper.selectById(request.getScheduleId());
            
            if (schedule == null) {
                log.warn("Schedule not found, scheduleId: {}", request.getScheduleId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在");
                return response;
            }
            
            // 2. 只能完成自己的日程
            if (!schedule.getDoctorId().equals(request.getDoctorId())) {
                log.warn("Permission denied: doctor {} tried to complete schedule {} owned by doctor {}", 
                        request.getDoctorId(), request.getScheduleId(), schedule.getDoctorId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权操作该日程");
                return response;
            }
            
            // 3. 只能完成待处理或进行中的日程
            if (ScheduleStatus.COMPLETED.getCode().equals(schedule.getStatus())) {
                log.warn("Schedule already completed, scheduleId: {}", request.getScheduleId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程已完成,无需重复操作");
                return response;
            }
            
            if (ScheduleStatus.CANCELLED.getCode().equals(schedule.getStatus())) {
                log.warn("Cannot complete cancelled schedule, scheduleId: {}", request.getScheduleId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("已取消的日程无法完成");
                return response;
            }
            
            // 4. 诊断报告不能为空
            if (request.getDiagnosisReport() == null || request.getDiagnosisReport().trim().isEmpty()) {
                log.warn("Diagnosis report is empty, scheduleId: {}", request.getScheduleId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("诊断报告不能为空");
                return response;
            }
            
            // 5. 更新日程状态和诊断报告
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, request.getScheduleId())
                   .eq(DoctorSchedule::getVersion, schedule.getVersion())  // 乐观锁
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
                log.warn("Complete schedule failed due to version conflict, scheduleId: {}", request.getScheduleId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("完成日程失败,请刷新后重试");
            }
        } catch (Exception e) {
            log.error("Complete schedule failed, scheduleId: {}, doctorId: {}", 
                    request.getScheduleId(), request.getDoctorId(), e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("完成日程失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancelScheduleResponse cancelSchedule(Long scheduleId, Long doctorId, String reason) {
        CancelScheduleResponse response = new CancelScheduleResponse();
        
        try {
            // 1. 查询日程
            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            
            if (schedule == null) {
                log.warn("Schedule not found, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在");
                return response;
            }
            
            // 2. 只能取消自己的日程
            if (!schedule.getDoctorId().equals(doctorId)) {
                log.warn("Permission denied: doctor {} tried to cancel schedule {} owned by doctor {}", 
                        doctorId, scheduleId, schedule.getDoctorId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权操作该日程");
                return response;
            }
            
            // 3. 只能取消待处理或进行中的日程
            if (ScheduleStatus.COMPLETED.getCode().equals(schedule.getStatus())) {
                log.warn("Cannot cancel completed schedule, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("已完成的日程无法取消");
                return response;
            }
            
            if (ScheduleStatus.CANCELLED.getCode().equals(schedule.getStatus())) {
                log.warn("Schedule already cancelled, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程已取消,无需重复操作");
                return response;
            }
            
            // 4. 验证取消原因不能为空
            if (reason == null || reason.trim().isEmpty()) {
                log.warn("Cancel reason is empty, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("取消原因不能为空");
                return response;
            }
            
            // 5. 更新日程状态
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, scheduleId)
                   .eq(DoctorSchedule::getVersion, schedule.getVersion())
                   .set(DoctorSchedule::getStatus, ScheduleStatus.CANCELLED.getCode())
                   .set(DoctorSchedule::getResult, "取消原因: " + reason)
                   .set(DoctorSchedule::getUpdateTime, new Date());
            
            int rows = scheduleMapper.update(null, wrapper);
            
            if (rows > 0) {
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("取消日程成功");
                response.setScheduleId(scheduleId);
                
                log.info("Cancel schedule success, scheduleId: {}, doctorId: {}, reason: {}", 
                        scheduleId, doctorId, reason);
            } else {
                log.warn("Cancel schedule failed due to version conflict, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("取消日程失败,请刷新后重试");
            }
        } catch (Exception e) {
            log.error("Cancel schedule failed, scheduleId: {}, doctorId: {}", scheduleId, doctorId, e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("取消日程失败: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateScheduleStatusResponse updateScheduleStatus(Long scheduleId, Long doctorId, String status) {
        UpdateScheduleStatusResponse response = new UpdateScheduleStatusResponse();
        
        try {
            // 1. 验证状态合法性
            ScheduleStatus targetStatus;
            try {
                targetStatus = ScheduleStatus.fromCode(status);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid schedule status: {}", status);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无效的日程状态: " + status);
                return response;
            }
            
            // 2. 查询日程
            DoctorSchedule schedule = scheduleMapper.selectById(scheduleId);
            
            if (schedule == null) {
                log.warn("Schedule not found, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("日程不存在");
                return response;
            }
            
            // 3. 业务级权限验证: 只能更新自己的日程
            if (!schedule.getDoctorId().equals(doctorId)) {
                log.warn("Permission denied: doctor {} tried to update schedule {} owned by doctor {}", 
                        doctorId, scheduleId, schedule.getDoctorId());
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("无权操作该日程");
                return response;
            }
            
            // 4. 验证状态流转合法性
            ScheduleStatus currentStatus = ScheduleStatus.fromCode(schedule.getStatus());
            if (!isValidStatusTransition(currentStatus, targetStatus)) {
                log.warn("Invalid status transition from {} to {}, scheduleId: {}", 
                        currentStatus, targetStatus, scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage(String.format("无效的状态流转: %s -> %s", 
                        currentStatus.getDescription(), targetStatus.getDescription()));
                return response;
            }
            
            // 5. 更新日程状态
            LambdaUpdateWrapper<DoctorSchedule> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DoctorSchedule::getId, scheduleId)
                   .eq(DoctorSchedule::getVersion, schedule.getVersion())  // 乐观锁
                   .set(DoctorSchedule::getStatus, status)
                   .set(DoctorSchedule::getUpdateTime, new Date());
            
            int rows = scheduleMapper.update(null, wrapper);
            
            if (rows > 0) {
                response.setCode(ToBCodeEnum.SUCCESS);
                response.setMessage("更新状态成功");
                response.setScheduleId(scheduleId);
                response.setStatus(status);
                
                log.info("Update schedule status success, scheduleId: {}, doctorId: {}, {} -> {}", 
                        scheduleId, doctorId, currentStatus, targetStatus);
            } else {
                log.warn("Update schedule status failed due to version conflict, scheduleId: {}", scheduleId);
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("更新状态失败,请刷新后重试");
            }
        } catch (Exception e) {
            log.error("Update schedule status failed, scheduleId: {}, doctorId: {}, status: {}", 
                    scheduleId, doctorId, status, e);
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("更新状态失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 验证日程状态流转是否合法
     * 
     * 状态流转规则:
     * PENDING -> IN_PROGRESS, COMPLETED, CANCELLED
     * IN_PROGRESS -> COMPLETED, CANCELLED
     * COMPLETED -> (不允许流转)
     * CANCELLED -> (不允许流转)
     */
    private boolean isValidStatusTransition(ScheduleStatus from, ScheduleStatus to) {
        if (from == to) {
            return true;  // 允许设置为当前状态(幂等)
        }
        
        switch (from) {
            case PENDING:
                // 待处理可以转换为任何状态
                return to == ScheduleStatus.IN_PROGRESS 
                    || to == ScheduleStatus.COMPLETED 
                    || to == ScheduleStatus.CANCELLED;
                
            case IN_PROGRESS:
                // 进行中只能转换为已完成或已取消
                return to == ScheduleStatus.COMPLETED 
                    || to == ScheduleStatus.CANCELLED;
                
            case COMPLETED:
            case CANCELLED:
                // 已完成和已取消的日程不允许再流转
                return false;
                
            default:
                return false;
        }
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

        DoctorVO doctorVO = doctorClient.getDoctorInfo(GetDoctorInfoRequest.builder()
                        .userId(schedule.getDoctorId())
                        .build());
        PatientVO patientVO = doctorClient.getPatientInfo(GetPatientInfoRequest.builder()
                        .userId(schedule.getPatientId())
                        .build());
        vo.setDoctorName(doctorVO.getUsername());
        vo.setPatientName(patientVO.getUsername());

        return vo;
    }
    
    /**
     * 生成模拟日程 (后续替换为AI服务)
     */
    private List<DoctorSchedule> generateSchedules(GenerateScheduleRequest request) {
        
        return new ArrayList<DoctorSchedule>();
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
