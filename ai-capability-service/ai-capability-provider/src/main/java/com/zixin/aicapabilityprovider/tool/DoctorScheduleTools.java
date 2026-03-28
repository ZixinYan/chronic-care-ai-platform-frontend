package com.zixin.aicapabilityprovider.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zixin.accountapi.api.UserIdentityAPI;
import com.zixin.accountapi.dto.GetDoctorInfoResponse;
import com.zixin.accountapi.vo.DoctorVO;
import com.zixin.doctorapi.api.DoctorWorkbenchAPI;
import com.zixin.doctorapi.dto.QueryScheduleRequest;
import com.zixin.doctorapi.dto.QueryScheduleResponse;
import com.zixin.doctorapi.vo.ScheduleVO;
import com.zixin.utils.utils.PageUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class DoctorScheduleTools {

    private final Gson gson = new GsonBuilder().create();

    @DubboReference(timeout = 50000)
    private UserIdentityAPI userIdentityAPI;

    @DubboReference(timeout = 50000)
    private DoctorWorkbenchAPI doctorWorkbenchAPI;

    public DoctorScheduleTools() {
    }

    /**
     * 查询指定日期下医生的出勤与画像信息，用于 AI 智能排班。
     */
    @Tool(name = "queryDoctorAvailabilityForDay", description = "根据预约日期查询所有有排班记录的医生出勤情况与画像信息，返回 JSON，用于智能排班推荐")
    public String queryDoctorAvailabilityForDay(String scheduleDay, String businessRequirement) {
        log.info("[Tool] queryDoctorAvailabilityForDay, scheduleDay={}, businessRequirement={}", scheduleDay, businessRequirement);

        String normalizedDay = normalizeScheduleDay(scheduleDay);
        if (normalizedDay == null || normalizedDay.isEmpty()) {
            return "{\"error\":\"scheduleDay is required\"}";
        }

        try {
            // 1. 查询该日所有日程记录（按日跨医生，doctorId 可为空）
            List<ScheduleVO> schedules = querySchedulesByDay(normalizedDay);
            
            // 2. 按医生聚合，并补齐医生画像信息
            List<DoctorAvailability> doctorAvailabilities = aggregateDoctorAvailability(schedules);
            
            // 3. 构建结果并返回
            DoctorAvailabilityResult result = new DoctorAvailabilityResult(normalizedDay, businessRequirement, doctorAvailabilities);
            return gson.toJson(result);
        } catch (Exception e) {
            log.error("Failed to query doctor availability", e);
            return gson.toJson(new DoctorAvailabilityResult(normalizedDay, businessRequirement, new ArrayList<>()));
        }
    }

    /**
     * 查询指定医生在某天的详细排班信息（JSON）。
     */
    @Tool(name = "queryDoctorScheduleDetailForDay", description = "根据医生ID和日期查询医生当日的详细排班信息，返回 JSON 列表")
    public String queryDoctorScheduleDetailForDay(Long doctorId, String scheduleDay) {
        log.info("[Tool] queryDoctorScheduleDetailForDay, doctorId={}, scheduleDay={}", doctorId, scheduleDay);
        String normalizedDay = normalizeScheduleDay(scheduleDay);
        if (doctorId == null || normalizedDay == null || normalizedDay.isEmpty()) {
            return "{\"error\":\"doctorId and scheduleDay are required\"}";
        }

        try {
            // 查询指定医生在指定日期的日程
            List<ScheduleVO> schedules = querySchedulesByDoctorAndDay(doctorId, normalizedDay);
            return gson.toJson(schedules);
        } catch (Exception e) {
            log.error("Failed to query doctor schedule detail", e);
            return gson.toJson(new ArrayList<>());
        }
    }

    /**
     * 根据日期查询所有日程
     */
    /**
     * 将 yyyyMMdd、yyyy-MM-dd 等统一为库表使用的 yyyy-MM-dd。
     */
    private static String normalizeScheduleDay(String scheduleDay) {
        if (scheduleDay == null) {
            return null;
        }
        String s = scheduleDay.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.matches("\\d{8}")) {
            return s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8);
        }
        return s;
    }

    private List<ScheduleVO> querySchedulesByDay(String scheduleDay) {
        QueryScheduleRequest request = new QueryScheduleRequest();
        request.setScheduleDay(scheduleDay);
        request.setPageSize(1000);
        
        QueryScheduleResponse response = doctorWorkbenchAPI.querySchedule(request);
        return extractSchedulesFromResponse(response);
    }

    /**
     * 根据医生ID和日期查询日程
     */
    private List<ScheduleVO> querySchedulesByDoctorAndDay(Long doctorId, String scheduleDay) {
        QueryScheduleRequest request = new QueryScheduleRequest();
        request.setDoctorId(doctorId);
        request.setScheduleDay(scheduleDay);
        request.setPageSize(1000);
        
        QueryScheduleResponse response = doctorWorkbenchAPI.querySchedule(request);
        return extractSchedulesFromResponse(response);
    }

    /**
     * 从响应中提取日程列表
     */
    private List<ScheduleVO> extractSchedulesFromResponse(QueryScheduleResponse response) {
        if (response == null || response.getSchedules() == null) {
            return new ArrayList<>();
        }
        
        PageUtils pageUtils = response.getSchedules();
        if (pageUtils.getList() == null) {
            return new ArrayList<>();
        }
        
        return pageUtils.getList().stream()
                .filter(item -> item instanceof ScheduleVO)
                .map(item -> (ScheduleVO) item)
                .collect(Collectors.toList());
    }

    /**
     * 聚合医生出勤信息
     */
    private List<DoctorAvailability> aggregateDoctorAvailability(List<ScheduleVO> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按医生ID分组
        return schedules.stream()
                .collect(Collectors.groupingBy(this::getDoctorIdFromSchedule))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> buildDoctorAvailability(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 从ScheduleVO中获取医生ID
     */
    private Long getDoctorIdFromSchedule(ScheduleVO schedule) {
        return schedule != null ? schedule.getDoctorId() : null;
    }

    /**
     * 构建医生出勤信息
     */
    private DoctorAvailability buildDoctorAvailability(Long doctorId, List<ScheduleVO> schedules) {
        if (doctorId == null) {
            return null;
        }

        // 获取医生信息
        DoctorInfo doctorInfo = getDoctorInfo(doctorId);
        
        // 统计日程状态
        ScheduleStats stats = calculateScheduleStats(schedules);
        
        // 构建并返回医生出勤信息
        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctorId(doctorId);
        availability.setDoctorName(doctorInfo.getName());
        availability.setDepartment(doctorInfo.getDepartment());
        availability.setTitle(doctorInfo.getTitle());
        availability.setExperience(doctorInfo.getExperience());
        availability.setTotalSchedules(stats.getTotal());
        availability.setPendingSchedules(stats.getPending());
        availability.setInProgressSchedules(stats.getInProgress());

        return availability;
    }

    /**
     * 获取医生信息
     */
    private DoctorInfo getDoctorInfo(Long doctorId) {
        DoctorInfo doctorInfo = new DoctorInfo();
        
        try {
            GetDoctorInfoResponse response = userIdentityAPI.getDoctorInfoByUserId(doctorId);
            if (response != null && response.getDoctor() != null) {
                DoctorVO doctor = response.getDoctor();
                doctorInfo.setName(doctor.getUsername());
                doctorInfo.setDepartment(doctor.getDepartment());
                doctorInfo.setTitle(doctor.getTitle());
                doctorInfo.setExperience(doctor.getExperience());
            }
        } catch (Exception e) {
            log.warn("Failed to load doctor info from UserIdentityAPI, doctorId={}", doctorId, e);
        }
        
        return doctorInfo;
    }

    /**
     * 计算日程统计信息
     */
    private ScheduleStats calculateScheduleStats(List<ScheduleVO> schedules) {
        ScheduleStats stats = new ScheduleStats();
        stats.setTotal(schedules.size());
        
        long pending = schedules.stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                .count();
        stats.setPending((int) pending);
        
        long inProgress = schedules.stream()
                .filter(s -> "IN_PROGRESS".equalsIgnoreCase(s.getStatus()))
                .count();
        stats.setInProgress((int) inProgress);
        
        return stats;
    }

    /**
     * 医生信息封装类
     */
    @Data
    private static class DoctorInfo {
        private String name;
        private String department;
        private String title;
        private Integer experience;
    }

    /**
     * 日程统计信息封装类
     */
    @Data
    private static class ScheduleStats {
        private int total;
        private int pending;
        private int inProgress;
    }

    /**
     * 工具返回的医生出勤聚合信息。
     */
    @Data
    public static class DoctorAvailability {
        private Long doctorId;
        private String doctorName;
        private String department;
        private String title;
        private Integer experience;
        private Integer totalSchedules;
        private Integer pendingSchedules;
        private Integer inProgressSchedules;
    }

    /**
     * 工具顶层返回对象，包含某日的所有候选医生信息。
     */
    @Data
    public static class DoctorAvailabilityResult {
        private String scheduleDay;
        private String businessRequirement;
        private List<DoctorAvailability> doctors;

        public DoctorAvailabilityResult(String scheduleDay, String businessRequirement, List<DoctorAvailability> doctors) {
            this.scheduleDay = scheduleDay;
            this.businessRequirement = businessRequirement;
            this.doctors = doctors;
        }
    }

}
