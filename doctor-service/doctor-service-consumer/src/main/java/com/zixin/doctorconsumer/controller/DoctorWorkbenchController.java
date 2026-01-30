package com.zixin.doctorconsumer.controller;

import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.*;
import com.zixin.doctorapi.vo.DoctorVO;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.exception.ToBCodeEnum;
import com.zixin.utils.security.RequirePermission;
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
 * 6. 获取医生信息
 * 
 * 权限设计:
 * - 类级别: @RequireRole("DOCTOR") - 只有医生角色可访问
 * - 方法级别: @RequirePermission - 细粒度权限控制
 * 
 * 安全策略:
 * - 所有接口从Gateway注入的X-User-Id Header获取用户ID
 * - 不信任请求参数中的doctorId,统一使用JWT中的userId
 * - Provider层会进一步验证数据归属权限
 * 
 * @author zixin
 */
@RestController
@RequestMapping("/doctor/workbench")
@Slf4j
@RequireRole("DOCTOR")  // 类级别权限: 只有DOCTOR角色可以访问
public class DoctorWorkbenchController {
    
    @DubboReference(check = false)
    private DoctorWorkbenchAPI workbenchAPI;
    
    /**
     * AI生成日程建议
     * 
     * 医生登录后，AI根据后台数据生成日程表建议
     * 
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:write (医生写权限)
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID,不使用请求参数中的doctorId
     * - 防止医生为其他医生生成日程
     *
     * @param request 生成日程请求
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return AI推荐的日程列表
     */
    @PostMapping("/schedule/generate")
    @RequirePermission("doctor:write")
    public Result<GenerateScheduleResponse> generateSchedule(
            @RequestBody GenerateScheduleRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        
        // 使用JWT中的userId,不信任请求参数
        request.setDoctorId(userId);
        
        log.info("Generate schedule request, doctorId: {}, date: {}", 
                userId, request.getScheduleDay());
        
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
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:read (医生读权限)
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - 医生只能查看自己的日程列表
     *
     * @param request 查询条件
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 日程列表
     */
    @GetMapping("/schedule/list")
    @RequirePermission("doctor:read")
    public Result<QueryScheduleResponse> querySchedule(
            @ModelAttribute QueryScheduleRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        
        // 强制使用JWT中的userId,防止越权查询
        request.setDoctorId(userId);
        
        log.info("Query schedule request, doctorId: {}, date: {}", 
                userId, request.getScheduleDay());
        
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
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:read (医生读权限)
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - Provider层会验证日程归属,防止查看其他医生的日程
     *
     * @param scheduleId 日程ID
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 日程详情
     */
    @GetMapping("/schedule/detail")
    @RequirePermission("doctor:read")
    public Result<ScheduleVO> getScheduleDetail(
            @RequestParam Long scheduleId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Get schedule detail, scheduleId: {}, doctorId: {}", scheduleId, userId);
        
        // Provider层会验证schedule.doctorId == userId
        GetScheduleDetailResponse response = workbenchAPI.getScheduleDetail(scheduleId, userId);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response.getSchedule());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 完成日程并上传诊断报告
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:write (医生写权限)
     * 业务规则:
     * - 只能完成自己的日程
     * - 诊断报告不能为空
     * - 只能完成待处理或进行中的日程
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - Provider层会验证日程归属和状态
     *
     * @param request 完成日程请求
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 完成结果
     */
    @PostMapping("/schedule/complete")
    @RequirePermission("doctor:write")
    public Result<CompleteScheduleResponse> completeSchedule(
            @RequestBody CompleteScheduleRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        // 使用JWT中的userId
        request.setDoctorId(userId);
        log.info("Complete schedule request, scheduleId: {}, doctorId: {}", 
                request.getScheduleId(), userId);
        
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
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:write (医生写权限)
     * 业务规则:
     * - 只能取消自己的日程
     * - 取消原因不能为空
     * - 只能取消待处理或进行中的日程
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - Provider层会验证日程归属和状态
     *
     * @param scheduleId 日程ID
     * @param reason 取消原因
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 取消结果
     */
    @PostMapping("/schedule/cancel")
    @RequirePermission("doctor:write")
    public Result<Boolean> cancelSchedule(
            @RequestParam Long scheduleId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Cancel schedule request, scheduleId: {}, doctorId: {}, reason: {}", 
                scheduleId, userId, reason);
        
        // Provider层会验证日程归属
        CancelScheduleResponse response = workbenchAPI.cancelSchedule(scheduleId, userId, reason);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(true);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 更新日程状态
     * 
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:write (医生写权限)
     * 
     * 业务规则:
     * - 只能更新自己的日程
     * - 状态流转规则: PENDING -> IN_PROGRESS -> COMPLETED/CANCELLED
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - Provider层会验证日程归属和状态流转
     *
     * @param scheduleId 日程ID
     * @param status 新状态 (PENDING/IN_PROGRESS/COMPLETED/CANCELLED)
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 更新结果
     */
    @PostMapping("/schedule/status")
    @RequirePermission("doctor:write")
    public Result<Boolean> updateScheduleStatus(
            @RequestParam Long scheduleId,
            @RequestParam String status,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Update schedule status, scheduleId: {}, doctorId: {}, status: {}", 
                scheduleId, userId, status);
        
        // Provider层会验证日程归属和状态流转
        UpdateScheduleStatusResponse response = workbenchAPI.updateScheduleStatus(scheduleId, userId, status);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(true);
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    /**
     * 获取医生信息
     * 
     * 权限要求:
     * - 角色: DOCTOR
     * - 权限: doctor:read (医生读权限)
     * 
     * 业务规则:
     * - 医生可以查看自己的详细信息
     * - 返回医生的基础信息、科室、职称、工作经验等
     * 
     * 安全策略:
     * - 从X-User-Id Header获取医生ID
     * - 查询当前登录医生的信息
     *
     * @param userId 从Gateway注入的用户ID (医生ID)
     * @return 医生信息
     */
    @GetMapping("/info")
    @RequirePermission("doctor:read")
    public Result<DoctorVO> getDoctorInfo(@RequestHeader("X-User-Id") Long userId) {
        log.info("Get doctor info, doctorId: {}", userId);
        
        // 查询当前登录医生的信息
        GetDoctorInfoResponse response = workbenchAPI.getDoctorInfo(userId);
        
        if (response.getCode() == ToBCodeEnum.SUCCESS) {
            return Result.success(response.getDoctor());
        } else {
            return Result.error(response.getMessage());
        }
    }
}
