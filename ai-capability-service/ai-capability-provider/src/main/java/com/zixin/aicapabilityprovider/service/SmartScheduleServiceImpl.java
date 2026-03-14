package com.zixin.aicapabilityprovider.service;

import com.zixin.aicapabilityapi.api.AIScheduleAPI;
import com.zixin.aicapabilityapi.dto.AiScheduleResult;
import com.zixin.aicapabilityapi.dto.GenerateScheduleRequest;
import com.zixin.aicapabilityapi.dto.GenerateScheduleResponse;
import com.zixin.aicapabilityapi.vo.SuggestScheduleVO;
import com.zixin.aicapabilityprovider.skill.SkillOrchestrator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import com.zixin.utils.exception.ToBCodeEnum;

import java.util.Collections;
import java.util.List;

@Service
public class SmartScheduleServiceImpl implements AIScheduleAPI {

    private final SkillOrchestrator skillOrchestrator;
    private final Gson gson = new GsonBuilder().create();

    public SmartScheduleServiceImpl(SkillOrchestrator skillOrchestrator) {
        this.skillOrchestrator = skillOrchestrator;
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

        String modelResponse;
        try {
            modelResponse = skillOrchestrator.executeWithSkillSelection(
                    request.getScheduleDay(),
                    request.getBusinessRequirement(),
                    request.getSpecifyDoctor(),
                    request.getDoctorId()
            );
        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("调用 AI 智能排班失败: " + e.getMessage());
            response.setRecommendedSchedules(Collections.emptyList());
            response.setRecommendation("AI 调用异常");
            return response;
        }

        List<SuggestScheduleVO> schedules = Collections.emptyList();
        String recommendation = null;
        try {
            AiScheduleResult result = gson.fromJson(modelResponse, AiScheduleResult.class);
            if (result != null && result.getSchedules() != null) {
                schedules = result.getSchedules();
            }
            recommendation = result != null ? result.getRecommendation() : null;
        } catch (Exception e) {
            recommendation = modelResponse;
        }

        if (schedules == null) {
            schedules = Collections.emptyList();
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

}
