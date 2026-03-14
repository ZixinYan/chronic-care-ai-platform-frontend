package com.zixin.aicapabilityprovider.agent;

import com.zixin.aicapabilityprovider.tool.DoctorScheduleTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleAgent {

    @Bean
    public MethodToolCallbackProvider doctorScheduleTools(DoctorScheduleTools scheduleTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(scheduleTools)
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient
                .builder(chatModel)
                .build();
    }
}
