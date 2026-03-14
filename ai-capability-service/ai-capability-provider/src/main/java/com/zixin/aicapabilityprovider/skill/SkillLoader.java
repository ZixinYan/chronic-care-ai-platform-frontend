package com.zixin.aicapabilityprovider.skill;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class SkillLoader {

    private static final String SKILLS_BASE_PATH = "classpath:skills/";
    private static final String SKILLS_DIR = "skills/";
    private static final String TOOLS_DIR = "tools/";

    private static final Pattern YAML_METADATA_PATTERN = Pattern.compile(
            "```yaml\\s*\\n([\\s\\S]*?)\\n```", Pattern.MULTILINE
    );

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private String baseSkill;
    private final Map<String, SkillDefinition> skills = new ConcurrentHashMap<>();
    private final Map<String, String> toolDefinitions = new ConcurrentHashMap<>();

    @Data
    public static class SkillDefinition {
        private String skillId;
        private String version;
        private String description;
        private List<String> toolDependencies = new ArrayList<>();
        private List<String> triggers = new ArrayList<>();
        private String fullContent;
    }

    @PostConstruct
    public void init() {
        loadBaseSkill();
        loadAllSkills();
        loadAllTools();
        log.info("SkillLoader initialized: skills={}, tools={}", skills.size(), toolDefinitions.size());
    }

    private void loadBaseSkill() {
        try {
            Resource resource = resolver.getResource(SKILLS_BASE_PATH + "base.md");
            if (resource.exists()) {
                baseSkill = loadResourceContent(resource);
                log.info("Loaded base skill");
            }
        } catch (IOException e) {
            log.warn("Failed to load base skill: {}", e.getMessage());
        }
    }

    private void loadAllSkills() {
        try {
            Resource[] resources = resolver.getResources(SKILLS_BASE_PATH + SKILLS_DIR + "*.md");
            for (Resource resource : resources) {
                String content = loadResourceContent(resource);
                SkillDefinition definition = parseSkillDefinition(content);
                if (definition != null && definition.getSkillId() != null) {
                    definition.setFullContent(content);
                    skills.put(definition.getSkillId(), definition);
                    log.info("Loaded skill: {} with tools: {}", 
                            definition.getSkillId(), definition.getToolDependencies());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load skills: {}", e.getMessage());
        }
    }

    private void loadAllTools() {
        try {
            Resource[] resources = resolver.getResources(SKILLS_BASE_PATH + TOOLS_DIR + "*.md");
            for (Resource resource : resources) {
                String filename = extractFilename(resource);
                String content = loadResourceContent(resource);
                toolDefinitions.put(filename, content);
                log.debug("Loaded tool: {}", filename);
            }
        } catch (IOException e) {
            log.warn("Failed to load tools: {}", e.getMessage());
        }
    }

    private SkillDefinition parseSkillDefinition(String content) {
        Matcher matcher = YAML_METADATA_PATTERN.matcher(content);
        if (!matcher.find()) {
            return null;
        }

        String yamlContent = matcher.group(1);
        SkillDefinition definition = new SkillDefinition();

        String[] lines = yamlContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("skillId:")) {
                definition.setSkillId(extractValue(line));
            } else if (line.startsWith("version:")) {
                definition.setVersion(extractValue(line));
            } else if (line.startsWith("description:")) {
                definition.setDescription(extractValue(line));
            } else if (line.startsWith("- ") && isToolDependencySection(yamlContent, line)) {
                definition.getToolDependencies().add(line.substring(2).trim());
            } else if (line.startsWith("- ") && isTriggerSection(yamlContent, line)) {
                definition.getTriggers().add(line.substring(2).trim());
            }
        }

        return definition;
    }

    private boolean isToolDependencySection(String yaml, String currentLine) {
        int toolDepIndex = yaml.indexOf("toolDependencies:");
        int triggerIndex = yaml.indexOf("triggers:");
        int lineIndex = yaml.indexOf(currentLine);
        
        if (toolDepIndex < 0) return false;
        if (triggerIndex < 0) return lineIndex > toolDepIndex;
        return lineIndex > toolDepIndex && lineIndex < triggerIndex;
    }

    private boolean isTriggerSection(String yaml, String currentLine) {
        int triggerIndex = yaml.indexOf("triggers:");
        int lineIndex = yaml.indexOf(currentLine);
        return triggerIndex >= 0 && lineIndex > triggerIndex;
    }

    private String extractValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex >= 0 && colonIndex < line.length() - 1) {
            return line.substring(colonIndex + 1).trim();
        }
        return "";
    }

    private String loadResourceContent(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private String extractFilename(Resource resource) {
        try {
            String filename = resource.getFilename();
            return filename != null ? filename.replace(".md", "") : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getBaseSkill() {
        return baseSkill;
    }

    public SkillDefinition getSkillDefinition(String skillId) {
        return skills.get(skillId);
    }

    public String getSkillContent(String skillId) {
        SkillDefinition definition = skills.get(skillId);
        return definition != null ? definition.getFullContent() : null;
    }

    public List<String> getSkillToolDependencies(String skillId) {
        SkillDefinition definition = skills.get(skillId);
        return definition != null ? definition.getToolDependencies() : Collections.emptyList();
    }

    public Map<String, SkillDefinition> getAllSkillDefinitions() {
        return Collections.unmodifiableMap(skills);
    }

    public String getToolDefinition(String toolName) {
        return toolDefinitions.get(toolName);
    }

    public String buildSkillOverview() {
        StringBuilder sb = new StringBuilder();
        sb.append("## 可用技能列表\n\n");
        sb.append("以下是当前可用的技能，请根据任务需要选择合适的技能：\n\n");
        
        for (Map.Entry<String, SkillDefinition> entry : skills.entrySet()) {
            SkillDefinition def = entry.getValue();
            sb.append("### ").append(def.getSkillId()).append("\n");
            sb.append("- **描述**: ").append(def.getDescription()).append("\n");
            sb.append("- **触发条件**: ").append(String.join("、", def.getTriggers())).append("\n");
            sb.append("- **依赖工具**: ").append(def.getToolDependencies().isEmpty() 
                    ? "无" : String.join(", ", def.getToolDependencies())).append("\n\n");
        }
        
        return sb.toString();
    }

    public String buildSkillDetailWithTools(String skillId) {
        SkillDefinition definition = skills.get(skillId);
        if (definition == null) {
            return "Skill not found: " + skillId;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## 技能详情: ").append(skillId).append("\n\n");
        sb.append(definition.getFullContent()).append("\n\n");

        List<String> toolDeps = definition.getToolDependencies();
        if (!toolDeps.isEmpty()) {
            sb.append("## 关联工具详情\n\n");
            for (String toolName : toolDeps) {
                String toolContent = toolDefinitions.get(toolName);
                if (toolContent != null) {
                    sb.append("### 工具: ").append(toolName).append("\n");
                    sb.append(toolContent).append("\n\n");
                } else {
                    sb.append("### 工具: ").append(toolName).append(" (定义文件未找到)\n\n");
                }
            }
        }

        return sb.toString();
    }

    public String buildInitialPrompt(String scheduleDay, String businessRequirement, 
                                      Boolean specifyDoctor, Long doctorId) {
        StringBuilder prompt = new StringBuilder();

        if (baseSkill != null) {
            prompt.append(baseSkill);
        }

        prompt.append("\n\n## 当前任务\n");
        prompt.append("- 预约日期: ").append(scheduleDay != null ? scheduleDay : "未指定").append("\n");
        prompt.append("- 业务需求: ").append(businessRequirement != null ? businessRequirement : "未明确").append("\n");
        prompt.append("- 是否指定医生: ").append(Boolean.TRUE.equals(specifyDoctor) ? "是" : "否").append("\n");
        if (Boolean.TRUE.equals(specifyDoctor) && doctorId != null) {
            prompt.append("- 指定医生ID: ").append(doctorId).append("\n");
        }

        prompt.append("\n\n").append(buildSkillOverview());

        prompt.append("\n## 技能选择指引\n");
        prompt.append("请根据上述任务，选择需要使用的技能。你可以：\n");
        prompt.append("1. 直接调用工具获取数据（工具会自动注册）\n");
        prompt.append("2. 按照技能描述的流程逐步执行\n");
        prompt.append("3. 综合多个技能的结果生成最终推荐\n");

        return prompt.toString();
    }

    public void reload() {
        log.info("Reloading all skills and tools...");
        skills.clear();
        toolDefinitions.clear();
        baseSkill = null;
        init();
    }
}
