package com.health.agent.module.knowledge.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库视图对象
 */
@Data
@Builder
public class KnowledgeBaseVO {
    private Long id;
    private String name;
    private String description;
    private String vectorDbType;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

