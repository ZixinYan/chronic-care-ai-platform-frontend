package com.zixin.aicapabilityprovider.skill;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SkillOrchestrator {

    private final ChatClient chatClient;
    private final SkillLoader skillLoader;
    private final MethodToolCallbackProvider toolProvider;

    private final Map<String, ConversationContext> conversations = new ConcurrentHashMap<>();

    @Data
    public static class ConversationContext {
        private String sessionId;
        private List<String> selectedSkills = new ArrayList<>();
        private List<String> calledTools = new ArrayList<>();
        private List<Message> messageHistory = new ArrayList<>();
        private Map<String, Object> contextData = new HashMap<>();
    }

    @Data
    public static class Message {
        private String role;
        private String content;
        private long timestamp;
    }

    public SkillOrchestrator(ChatClient chatClient, 
                             SkillLoader skillLoader,
                             MethodToolCallbackProvider toolProvider) {
        this.chatClient = chatClient;
        this.skillLoader = skillLoader;
        this.toolProvider = toolProvider;
    }

    public String executeWithSkillSelection(String scheduleDay, 
                                             String businessRequirement,
                                             Boolean specifyDoctor, 
                                             Long doctorId) {
        String sessionId = UUID.randomUUID().toString();
        ConversationContext context = new ConversationContext();
        context.setSessionId(sessionId);
        conversations.put(sessionId, context);

        String initialPrompt = skillLoader.buildInitialPrompt(
                scheduleDay, businessRequirement, specifyDoctor, doctorId
        );

        log.info("[Session: {}] Starting skill-based conversation", sessionId);
        log.debug("[Session: {}] Initial prompt length: {}", sessionId, initialPrompt.length());

        String response = chatClient.prompt()
                .user(initialPrompt)
                .tools(toolProvider)
                .call()
                .content();

        context.getCalledTools().addAll(extractToolCalls(response));

        log.info("[Session: {}] Conversation completed, tools called: {}", 
                sessionId, context.getCalledTools());

        return response;
    }

    public String executeWithExplicitSkills(String scheduleDay,
                                             String businessRequirement,
                                             Boolean specifyDoctor,
                                             Long doctorId,
                                             List<String> skillIds) {
        String sessionId = UUID.randomUUID().toString();
        ConversationContext context = new ConversationContext();
        context.setSessionId(sessionId);
        context.getSelectedSkills().addAll(skillIds);
        conversations.put(sessionId, context);

        StringBuilder prompt = new StringBuilder();

        if (skillLoader.getBaseSkill() != null) {
            prompt.append(skillLoader.getBaseSkill());
        }

        prompt.append("\n\n## 当前任务\n");
        prompt.append("- 预约日期: ").append(scheduleDay != null ? scheduleDay : "未指定").append("\n");
        prompt.append("- 业务需求: ").append(businessRequirement != null ? businessRequirement : "未明确").append("\n");
        prompt.append("- 是否指定医生: ").append(Boolean.TRUE.equals(specifyDoctor) ? "是" : "否").append("\n");
        if (Boolean.TRUE.equals(specifyDoctor) && doctorId != null) {
            prompt.append("- 指定医生ID: ").append(doctorId).append("\n");
        }

        prompt.append("\n\n## 指定技能\n");
        prompt.append("本次任务需要使用以下技能：\n\n");
        
        for (String skillId : skillIds) {
            prompt.append(skillLoader.buildSkillDetailWithTools(skillId)).append("\n\n");
        }

        log.info("[Session: {}] Executing with explicit skills: {}", sessionId, skillIds);

        String response = chatClient.prompt()
                .user(prompt.toString())
                .tools(toolProvider)
                .call()
                .content();

        context.getCalledTools().addAll(extractToolCalls(response));

        return response;
    }

    public String buildSkillAwarePrompt(String scheduleDay,
                                         String businessRequirement,
                                         Boolean specifyDoctor,
                                         Long doctorId) {
        StringBuilder prompt = new StringBuilder();

        if (skillLoader.getBaseSkill() != null) {
            prompt.append(skillLoader.getBaseSkill());
        }

        prompt.append("\n\n## 当前任务\n");
        prompt.append("- 预约日期: ").append(scheduleDay != null ? scheduleDay : "未指定").append("\n");
        prompt.append("- 业务需求: ").append(businessRequirement != null ? businessRequirement : "未明确").append("\n");
        prompt.append("- 是否指定医生: ").append(Boolean.TRUE.equals(specifyDoctor) ? "是" : "否").append("\n");
        if (Boolean.TRUE.equals(specifyDoctor) && doctorId != null) {
            prompt.append("- 指定医生ID: ").append(doctorId).append("\n");
        }

        prompt.append("\n\n").append(skillLoader.buildSkillOverview());

        prompt.append("\n## 工具调用说明\n");
        prompt.append("以下工具已注册，可直接调用：\n");
        
        Map<String, SkillLoader.SkillDefinition> allSkills = skillLoader.getAllSkillDefinitions();
        Set<String> allTools = new HashSet<>();
        for (SkillLoader.SkillDefinition def : allSkills.values()) {
            allTools.addAll(def.getToolDependencies());
        }
        
        for (String toolName : allTools) {
            prompt.append("- `").append(toolName).append("`\n");
        }

        prompt.append("\n## 执行指引\n");
        prompt.append("1. 分析任务需求，确定需要哪些数据\n");
        prompt.append("2. 调用相应的工具获取数据\n");
        prompt.append("3. 基于获取的数据进行分析和筛选\n");
        prompt.append("4. 生成最终的排班推荐（JSON格式）\n");

        return prompt.toString();
    }

    private List<String> extractToolCalls(String response) {
        List<String> tools = new ArrayList<>();
        if (response == null) return tools;

        Map<String, SkillLoader.SkillDefinition> allSkills = skillLoader.getAllSkillDefinitions();
        for (SkillLoader.SkillDefinition def : allSkills.values()) {
            for (String toolName : def.getToolDependencies()) {
                if (response.contains(toolName)) {
                    tools.add(toolName);
                }
            }
        }
        
        return tools;
    }

    public ConversationContext getContext(String sessionId) {
        return conversations.get(sessionId);
    }

    public void clearContext(String sessionId) {
        conversations.remove(sessionId);
    }

    public SkillLoader getSkillLoader() {
        return skillLoader;
    }
}
