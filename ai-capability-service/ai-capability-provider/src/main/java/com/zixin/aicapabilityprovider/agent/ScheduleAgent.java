package com.zixin.aicapabilityprovider.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * 智能排班：ChatClient **仅负责根据 Observation 推理**，不注册工具。
 * <p>
 * ReAct 中的 <b>Act（工具执行）</b>由 {@link com.zixin.aicapabilityprovider.service.SmartScheduleServiceImpl}
 * 在调用模型前完成，避免部分模型用自然语言伪造 {@code action}/{@code action_input} 而不走真实 function calling。
 */
@Configuration
public class ScheduleAgent {

    @Value("classpath:skills/smart-schedule-prompt.md")
    private Resource systemPromptResource;

    /**
     * 排班推理专用客户端（无 Tools / SkillsTool）。
     */
    @Bean("scheduleChatClient")
    ChatClient scheduleChatClient(ChatClient.Builder builder) throws Exception {
        String systemPrompt = StreamUtils.copyToString(
                systemPromptResource.getInputStream(),
                StandardCharsets.UTF_8
        );
        return builder.defaultSystem(systemPrompt).build();
    }
}
