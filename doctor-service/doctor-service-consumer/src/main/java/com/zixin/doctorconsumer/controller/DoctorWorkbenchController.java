package com.zixin.doctorconsumer.controller;

import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.*;
import com.zixin.doctorapi.vo.DoctorVO;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.security.RequireRole;
import com.zixin.utils.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * 医生工作台Controller
 * 
 * 提供给医生端的HTTP接口，使用Result作为统一返回体
 * 
 * 功能:
 * 1. AI生成日程建议
 * 2. 查看日程表
 * 3. 完成日程并上传诊断报告
 * 4. 取消日程
 * 5. 更新日程状态
 */
@RestController
@RequestMapping("/doctor/workbench")
@Slf4j
@RequireRole("DOCTOR")  // 只有医生角色可以访问
public class DoctorWorkbenchController {
    
    @DubboReference(check = false)
    private DoctorWorkbenchAPI workbenchAPI;
    
    /**
     * AI生成日程建议
     * 
     * 医生登录后，AI根据后台数据生成日程表建议
     *
     * @param request 生成日程请求
     * @return AI推荐的日程列表
     */
    @PostMapping("/schedule/generate")
    public Result<GenerateScheduleResponse> generateSchedule(@RequestBody GenerateScheduleRequest request) {
        log.info("Generate schedule request, doctorId: {}, date: {}", 
                request.getDoctorId(), request.getScheduleDay());
        
        GenerateScheduleResponse response = workbenchAPI.generateScheduleSuggestion(request);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 查看日程表
     *
     * @param request 查询条件
     * @return 日程列表
     */
    @GetMapping("/schedule/list")
    public Result<QueryScheduleResponse> querySchedule(@ModelAttribute QueryScheduleRequest request) {
        log.info("Query schedule request, doctorId: {}, date: {}", 
                request.getDoctorId(), request.getScheduleDay());
        
        QueryScheduleResponse response = workbenchAPI.querySchedule(request);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 获取日程详情
     *
     * @param scheduleId 日程ID
     * @param doctorId 医生ID
     * @return 日程详情
     */
    @GetMapping("/schedule/detail")
    public Result<ScheduleVO> getScheduleDetail(@RequestParam Long scheduleId, 
                                                 @RequestParam Long doctorId) {
        log.info("Get schedule detail, scheduleId: {}, doctorId: {}", scheduleId, doctorId);
        
        GetScheduleDetailResponse response = workbenchAPI.getScheduleDetail(scheduleId, doctorId);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response.getSchedule());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 完成日程并上传诊断报告
     *
     * @param request 完成日程请求
     * @return 完成结果
     */
    @PostMapping("/schedule/complete")
    public Result<CompleteScheduleResponse> completeSchedule(@RequestBody CompleteScheduleRequest request) {
        log.info("Complete schedule request, scheduleId: {}, doctorId: {}", 
                request.getScheduleId(), request.getDoctorId());
        
        CompleteScheduleResponse response = workbenchAPI.completeSchedule(request);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 取消日程
     *
     * @param scheduleId 日程ID
     * @param doctorId 医生ID
     * @param reason 取消原因
     * @return 取消结果
     */
    @PostMapping("/schedule/cancel")
    public Result<Boolean> cancelSchedule(@RequestParam Long scheduleId,
                                           @RequestParam Long doctorId,
                                           @RequestParam String reason) {
        log.info("Cancel schedule request, scheduleId: {}, doctorId: {}, reason: {}", 
                scheduleId, doctorId, reason);
        
        CancelScheduleResponse response = workbenchAPI.cancelSchedule(scheduleId, doctorId, reason);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(true);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 更新日程状态
     *
     * @param scheduleId 日程ID
     * @param doctorId 医生ID
     * @param status 新状态
     * @return 更新结果
     */
    @PostMapping("/schedule/status")
    public Result<Boolean> updateScheduleStatus(@RequestParam Long scheduleId,
                                                 @RequestParam Long doctorId,
                                                 @RequestParam String status) {
        log.info("Update schedule status, scheduleId: {}, doctorId: {}, status: {}", 
                scheduleId, doctorId, status);
        
        UpdateScheduleStatusResponse response = workbenchAPI.updateScheduleStatus(scheduleId, doctorId, status);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(true);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 获取医生信息
     *
     * @param doctorId 医生ID
     * @return 医生信息
     */
    @GetMapping("/info")
    public Result<DoctorVO> getDoctorInfo(@RequestParam Long doctorId) {
        log.info("Get doctor info, doctorId: {}", doctorId);
        
        GetDoctorInfoResponse response = workbenchAPI.getDoctorInfo(doctorId);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response.getDoctor());
        } else {
            return Result.error(response.getMessage());
        }
    }
}
