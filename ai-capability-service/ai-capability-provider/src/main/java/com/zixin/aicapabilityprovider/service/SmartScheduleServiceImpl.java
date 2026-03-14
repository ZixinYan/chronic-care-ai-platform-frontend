package com.zixin.aicapabilityprovider.service;

import com.zixin.aicapabilityapi.api.AIScheduleAPI;
import com.zixin.aicapabilityapi.dto.AiScheduleResult;
import com.zixin.aicapabilityapi.dto.GenerateScheduleRequest;
import com.zixin.aicapabilityapi.dto.GenerateScheduleResponse;
import com.zixin.aicapabilityapi.vo.SuggestScheduleVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.zixin.utils.exception.ToBCodeEnum;

import java.util.Collections;
import java.util.List;

@Service
@DubboService
public class SmartScheduleServiceImpl implements AIScheduleAPI {

    private final ChatClient chatClient;
    private final Gson gson = new GsonBuilder().create();

    public SmartScheduleServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
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

        String userPrompt = buildUserPrompt(request);

        String modelResponse;
        try {
            modelResponse = chatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
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

    private String buildUserPrompt(GenerateScheduleRequest request) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("请为以下排班需求生成推荐：\n\n");
        sb.append("**预约日期**: ").append(request.getScheduleDay()).append("\n");
        
        if (request.getBusinessRequirement() != null && !request.getBusinessRequirement().isEmpty()) {
            sb.append("**业务需求**: ").append(request.getBusinessRequirement()).append("\n");
        }
        
        if (Boolean.TRUE.equals(request.getSpecifyDoctor())) {
            sb.append("**是否指定医生**: 是\n");
            if (request.getDoctorId() != null) {
                sb.append("**指定医生ID**: ").append(request.getDoctorId()).append("\n");
            }
        } else {
            sb.append("**是否指定医生**: 否\n");
        }
        
        sb.append("\n请根据 smart-schedule-assistant 技能的指导，选择合适的技能组合，调用工具获取数据，并生成排班推荐。");
        
        return sb.toString();
    }

}
