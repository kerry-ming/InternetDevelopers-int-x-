package com.health.agent.module.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体
 */
@Data
public class KnowledgeBase {
    private Long id;
    private String name;
    private String description;
    private String vectorDbType;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

