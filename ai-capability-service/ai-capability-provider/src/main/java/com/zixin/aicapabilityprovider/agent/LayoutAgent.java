package com.zixin.aicapabilityprovider.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class LayoutAgent {

    private static final String LAYOUT_SKILLS_CLASSPATH = "skills/layout-skills";

    @Value("classpath:skills/smart-layout-prompt.md")
    private Resource systemPromptResource;

    @Value("${ai.capability.layout-skills-dir:}")
    private String layoutSkillsDir;

    /**
     * 智能排版编排专用 ChatClient，不挂载排班领域工具。
     */
    @Bean("layoutChatClient")
    ChatClient layoutChatClient(ChatClient.Builder builder) throws Exception {
        String systemPrompt = StreamUtils.copyToString(
                systemPromptResource.getInputStream(),
                StandardCharsets.UTF_8
        );

        String skillsPath = SkillsDirectoryResolver.resolve(layoutSkillsDir, LAYOUT_SKILLS_CLASSPATH);
        ChatClient.Builder configured = builder.defaultSystem(systemPrompt);
        if (skillsPath != null) {
            try {
                return configured
                        .defaultTools(
                                SkillsTool.builder()
                                        .addSkillsDirectory(skillsPath)
                                        .build()
                        )
                        .build();
            } catch (IllegalArgumentException e) {
                // spring-ai-agent-utils：目录存在但未解析出任何 skill 时会抛出 "At least one skill must be configured"
                log.warn(
                        "排版 SkillsTool 未加载（{}），将仅使用系统提示。可检查 layout-skills 下 Markdown  frontmatter 是否符合库要求。",
                        e.getMessage()
                );
            }
        }
        return configured.build();
    }
}
