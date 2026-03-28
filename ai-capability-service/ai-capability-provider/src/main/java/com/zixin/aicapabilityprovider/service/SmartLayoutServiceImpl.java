package com.zixin.aicapabilityprovider.service;

import com.zixin.aicapabilityapi.api.AILayoutAPI;
import com.zixin.aicapabilityapi.dto.AiLayoutResult;
import com.zixin.aicapabilityapi.dto.LayoutOrchestrateRequest;
import com.zixin.aicapabilityapi.dto.LayoutOrchestrateResponse;
import com.zixin.aicapabilityapi.vo.LayoutBlockVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zixin.utils.exception.ToBCodeEnum;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 智能排版编排：调用排版专用 {@link ChatClient}，解析模型 JSON 为强类型结果。
 */
@Service
@DubboService
public class SmartLayoutServiceImpl implements AILayoutAPI {

    private final ChatClient layoutChatClient;
    private final Gson gson = new GsonBuilder().create();

    /**
     * @param layoutChatClient 排版编排专用客户端
     */
    public SmartLayoutServiceImpl(@Qualifier("layoutChatClient") ChatClient layoutChatClient) {
        this.layoutChatClient = layoutChatClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutOrchestrateResponse orchestrateLayout(LayoutOrchestrateRequest request) {
        LayoutOrchestrateResponse response = new LayoutOrchestrateResponse();

        if (request == null || request.getSourceContent() == null || request.getSourceContent().trim().isEmpty()) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("待编排内容 sourceContent 不可为空");
            response.setBlocks(Collections.emptyList());
            response.setRationale("请求参数不完整");
            return response;
        }

        String userPrompt = buildUserPrompt(request);
        String modelResponse;
        try {
            modelResponse = layoutChatClient.prompt()
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("调用 AI 智能排版失败: " + e.getMessage());
            response.setBlocks(Collections.emptyList());
            response.setRationale("AI 调用异常");
            return response;
        }

        try {
            AiLayoutResult result = gson.fromJson(trimJsonNoise(modelResponse), AiLayoutResult.class);
            if (result == null || result.getBlocks() == null || result.getBlocks().isEmpty()) {
                response.setCode(ToBCodeEnum.FAIL);
                response.setMessage("AI 返回无法解析为有效的排版结构");
                response.setBlocks(Collections.emptyList());
                response.setRationale(result != null ? result.getRationale() : null);
                response.setRawModelOutput(modelResponse);
                return response;
            }
            List<LayoutBlockVO> blocks = result.getBlocks();
            response.setBlocks(blocks);
            response.setRationale(
                    result.getRationale() == null || result.getRationale().isEmpty()
                            ? "AI 已完成排版编排"
                            : result.getRationale()
            );
            response.setCode(ToBCodeEnum.SUCCESS);
            response.setMessage("智能排版编排成功");
            return response;
        } catch (Exception e) {
            response.setCode(ToBCodeEnum.FAIL);
            response.setMessage("解析 AI 排版结果失败: " + e.getMessage());
            response.setBlocks(Collections.emptyList());
            response.setRationale(modelResponse);
            response.setRawModelOutput(modelResponse);
            return response;
        }
    }

    private static String trimJsonNoise(String raw) {
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

    private String buildUserPrompt(LayoutOrchestrateRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下内容进行智能排版编排，并严格返回约定的纯 JSON（无 Markdown 代码块）：\n\n");
        sb.append("**sourceContent**:\n").append(request.getSourceContent()).append("\n\n");
        if (request.getContentType() != null && !request.getContentType().isEmpty()) {
            sb.append("**contentType**: ").append(request.getContentType()).append("\n");
        }
        if (request.getTargetFormat() != null && !request.getTargetFormat().isEmpty()) {
            sb.append("**targetFormat**: ").append(request.getTargetFormat()).append("\n");
        }
        if (request.getConstraints() != null && !request.getConstraints().isEmpty()) {
            sb.append("**constraints**: ").append(request.getConstraints()).append("\n");
        }
        sb.append("\n请根据 smart-layout-prompt 与 layout-skills 中的技能完成编排。");
        return sb.toString();
    }
}
