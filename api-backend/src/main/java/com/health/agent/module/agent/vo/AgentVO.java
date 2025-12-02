package com.health.agent.module.agent.vo;

import com.health.agent.module.agent.model.ModelConfig;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体视图对象
 */
@Data
@Builder
public class AgentVO {
    private Long id;
    private String name;
    private String description;
    private String systemPrompt;
    private String userPromptTemplate;
    private ModelConfig modelConfig;
    private Long workflowId;
    private List<Long> knowledgeBaseIds;
    private List<Long> pluginIds;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

