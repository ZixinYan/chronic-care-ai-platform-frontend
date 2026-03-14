package com.zixin.aicapabilityprovider.agent;

import com.zixin.aicapabilityprovider.tool.DoctorScheduleTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Configuration
public class ScheduleAgent {

    @Value("classpath:skills/smart-schedule-prompt.md")
    private Resource systemPromptResource;

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, DoctorScheduleTools doctorScheduleTools) throws Exception {
        String systemPrompt = StreamUtils.copyToString(
                systemPromptResource.getInputStream(), 
                StandardCharsets.UTF_8
        );

        return builder
                .defaultSystem(systemPrompt)
                .defaultTools(
                        SkillsTool.builder()
                                .addSkillsDirectory("classpath:skills/skills")
                                .build(),
                        doctorScheduleTools
                )
                .build();
    }
}
