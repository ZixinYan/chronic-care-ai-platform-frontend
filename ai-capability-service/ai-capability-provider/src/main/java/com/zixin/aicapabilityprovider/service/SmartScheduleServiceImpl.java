package com.zixin.aicapabilityprovider.service;

import com.zixin.aicapabilityapi.api.AIScheduleAPI;
import com.zixin.aicapabilityapi.dto.AiScheduleResult;
import com.zixin.aicapabilityapi.dto.GenerateScheduleRequest;
import com.zixin.aicapabilityapi.dto.GenerateScheduleResponse;
import com.zixin.aicapabilityapi.vo.SuggestScheduleVO;
import com.zixin.aicapabilityprovider.tool.DoctorScheduleTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.zixin.utils.exception.ToBCodeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 智能排班：采用 <b>Code-first ReAct</b>——在 Java 中顺序执行工具（Act）并将结果作为 Observation 注入用户消息，
 * 再调用无工具的 {@link ChatClient} 完成推理与 JSON 输出（Reason），避免模型伪造工具调用。
 */
@Slf4j
@Service
@DubboService
public class SmartScheduleServiceImpl implements AIScheduleAPI {

    private static final int MAX_DETAIL_LOOKUPS = 2;

    private final ChatClient chatClient;
    private final DoctorScheduleTools doctorScheduleTools;
    private final Gson gson = new GsonBuilder().create();

    public SmartScheduleServiceImpl(
            @Qualifier("scheduleChatClient") ChatClient chatClient,
            DoctorScheduleTools doctorScheduleTools) {
        this.chatClient = chatClient;
        this.doctorScheduleTools = doctorScheduleTools;
    }

    @Override
    public GenerateScheduleResponse SmartScheduleGenerate(GenerateScheduleRequest request) {
        GenerateScheduleResponse response = new GenerateScheduleResponse();

        if (request == null || request.getScheduleDay() == null || request.getScheduleDay().isEmpty()) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("预约日期不可为空");
            response.setRecommendedSchedules(Collections.emptyList());
            response.setRecommendation("请求参数不完整，缺少 scheduleDay");
            return response;
        }

        String toolScheduleDay = normalizeScheduleDayForDoctorDb(request.getScheduleDay());

        // Step 1: 查询所有医生信息
        String allDoctorsObservation = doctorScheduleTools.queryAllDoctors();
        log.debug("ReAct observation allDoctors (truncated): {}",
                allDoctorsObservation.length() > 500 ? allDoctorsObservation.substring(0, 500) + "…" : allDoctorsObservation);

        // Step 2: 查询医生请假情况（查询目标日期前后7天的请假记录）
        String leaveStartDay = calculateLeaveStartDay(toolScheduleDay, -7);
        String leaveEndDay = calculateLeaveStartDay(toolScheduleDay, 7);
        String leavesObservation = doctorScheduleTools.queryDoctorLeaves(leaveStartDay, leaveEndDay);
        log.debug("ReAct observation leaves (truncated): {}",
                leavesObservation.length() > 500 ? leavesObservation.substring(0, 500) + "…" : leavesObservation);

        // Step 3: 查询医生排班情况
        String availabilityObservation = doctorScheduleTools.queryDoctorAvailabilityForDay(
                toolScheduleDay,
                request.getBusinessRequirement() != null ? request.getBusinessRequirement() : ""
        );
        log.debug("ReAct observation availability (truncated): {}",
                availabilityObservation.length() > 500 ? availabilityObservation.substring(0, 500) + "…" : availabilityObservation);

        // Step 4: 查询候选医生的详细排班信息
        List<String> detailObservations = new ArrayList<>();
        for (Long doctorId : extractTopDoctorIds(availabilityObservation, MAX_DETAIL_LOOKUPS)) {
            String detailJson = doctorScheduleTools.queryDoctorScheduleDetailForDay(doctorId, toolScheduleDay);
            detailObservations.add(detailJson);
            log.debug("ReAct observation detail doctorId={} (truncated): {}",
                    doctorId,
                    detailJson.length() > 400 ? detailJson.substring(0, 400) + "…" : detailJson);
        }

        String userPrompt = buildUserPromptWithObservations(
                request, toolScheduleDay,
                allDoctorsObservation, leavesObservation, availabilityObservation, detailObservations
        );

        String modelResponse;
        try {
            modelResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
            log.info("modelResponse={}", modelResponse);
        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("调用 AI 智能排班失败: " + e.getMessage());
            response.setRecommendedSchedules(Collections.emptyList());
            response.setRecommendation("AI 调用异常");
            return response;
        }

        String jsonPayload = trimJsonFence(modelResponse);
        log.info("ReAct model response: {}", jsonPayload);
        List<SuggestScheduleVO> schedules = Collections.emptyList();
        String recommendation = null;
        try {
            AiScheduleResult result = gson.fromJson(jsonPayload, AiScheduleResult.class);
            if (result != null && result.getSchedules() != null) {
                schedules = result.getSchedules();
            }
            recommendation = result != null ? result.getRecommendation() : null;
        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("解析 AI 排班 JSON 失败: " + e.getMessage());
            response.setRecommendedSchedules(Collections.emptyList());
            response.setRecommendation(modelResponse);
            return response;
        }

        if (schedules == null) {
            schedules = Collections.emptyList();
        }

        boolean hasDoctorId = schedules.stream()
                .anyMatch(s -> s != null && s.getDoctorId() != null);
        if (schedules.isEmpty() || !hasDoctorId) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("AI 未返回有效推荐：至少需要一条包含 doctorId（医生用户 ID，须与 Observation 中 doctors[].doctorId 一致）的日程");
            response.setRecommendedSchedules(schedules);
            response.setRecommendation(
                    recommendation == null || recommendation.isEmpty() ? jsonPayload : recommendation
            );
            return response;
        }

        response.setRecommendedSchedules(schedules);
        response.setRecommendation(
                recommendation == null || recommendation.isEmpty()
                        ? "AI 已生成智能排班结果"
                        : recommendation
        );
        response.setCode(ToBCodeEnum.SUCCESS);
        response.setMessage("AI 智能日程推荐成功");
        return response;
    }

    /**
     * 计算请假查询的起始/结束日期
     */
    private String calculateLeaveStartDay(String scheduleDay, int offsetDays) {
        if (scheduleDay == null || scheduleDay.length() != 10) {
            return scheduleDay;
        }
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(scheduleDay);
            java.time.LocalDate resultDate = date.plusDays(offsetDays);
            return resultDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return scheduleDay;
        }
    }

    /**
     * 从 availability JSON 中取出前 {@code limit} 个 {@code doctorId}，用于可选的明细查询。
     */
    private static List<Long> extractTopDoctorIds(String availabilityJson, int limit) {
        List<Long> ids = new ArrayList<>();
        if (availabilityJson == null || availabilityJson.isEmpty() || limit <= 0) {
            return ids;
        }
        try {
            JsonObject root = JsonParser.parseString(availabilityJson).getAsJsonObject();
            if (root.has("error")) {
                return ids;
            }
            JsonArray doctors = root.getAsJsonArray("doctors");
            if (doctors == null) {
                return ids;
            }
            for (JsonElement el : doctors) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject d = el.getAsJsonObject();
                if (d.has("doctorId") && !d.get("doctorId").isJsonNull()) {
                    ids.add(d.get("doctorId").getAsLong());
                    if (ids.size() >= limit) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析 availability JSON 以提取 doctorId 失败: {}", e.getMessage());
        }
        return ids;
    }

    private String buildUserPromptWithObservations(
            GenerateScheduleRequest request,
            String toolScheduleDay,
            String allDoctorsObservation,
            String leavesObservation,
            String availabilityObservation,
            List<String> detailObservations) {

        StringBuilder sb = new StringBuilder();
        sb.append("## 排班任务\n\n");
        sb.append("**预约日期(上游原始值)**: ").append(request.getScheduleDay()).append("\n");
        sb.append("**工具用 scheduleDay（YYYY-MM-DD）**: ").append(toolScheduleDay).append("\n");

        if (request.getBusinessRequirement() != null && !request.getBusinessRequirement().isEmpty()) {
            sb.append("**业务需求**: ").append(request.getBusinessRequirement()).append("\n");
        }
        if (Boolean.TRUE.equals(request.getSpecifyDoctor())) {
            sb.append("**是否指定医生**: 是\n");
            if (request.getDoctorId() != null) {
                sb.append("**指定医生用户 ID**: ").append(request.getDoctorId()).append("\n");
            }
        } else {
            sb.append("**是否指定医生**: 否\n");
        }

        // Observation 1: 所有医生信息
        sb.append("\n## Observation · queryAllDoctors\n\n");
        sb.append("以下是系统中所有医生的基本信息，包括科室、职称、工作经验等，用于根据专业匹配度推荐：\n");
        sb.append(allDoctorsObservation).append("\n");

        // Observation 2: 医生请假情况
        sb.append("\n## Observation · queryDoctorLeaves\n\n");
        sb.append("以下是目标日期附近的已批准请假记录，排班时需排除这些医生的空闲时段：\n");
        sb.append(leavesObservation).append("\n");

        // Observation 3: 医生排班情况
        sb.append("\n## Observation · queryDoctorAvailabilityForDay\n\n");
        sb.append("以下是目标日期各医生的排班情况，包含已有日程数量和状态：\n");
        sb.append(availabilityObservation).append("\n");

        // Observation 4: 候选医生详细排班
        int idx = 1;
        for (String detail : detailObservations) {
            sb.append("\n## Observation · queryDoctorScheduleDetailForDay (候选 ").append(idx++).append(")\n\n");
            sb.append(detail).append("\n");
        }

        sb.append("\n---\n");
        sb.append("请根据以上 Observation 进行智能排班推荐：\n");
        sb.append("1. 优先根据业务需求匹配医生专业（科室、职称、经验）\n");
        sb.append("2. 排除请假期间的医生（检查 leaves 列表中是否有 doctorId 对应且日期重叠）\n");
        sb.append("3. 考虑医生当天的已有日程数量，优先推荐空闲医生\n");
        sb.append("4. **直接输出**最终纯 JSON（`schedules` + `recommendation`），不要描述调用工具，不要使用 Markdown 代码块。\n");
        return sb.toString();
    }

    private static String normalizeScheduleDayForDoctorDb(String scheduleDay) {
        if (scheduleDay == null) {
            return "";
        }
        String s = scheduleDay.trim();
        if (s.matches("\\d{8}")) {
            return s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8);
        }
        return s;
    }

    private static String trimJsonFence(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl > 0) {
                s = s.substring(firstNl + 1);
            }
            int endFence = s.lastIndexOf("```");
            if (endFence >= 0) {
                s = s.substring(0, endFence);
            }
            return s.trim();
        }
        return s;
    }
}
