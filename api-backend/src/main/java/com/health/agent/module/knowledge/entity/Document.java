package com.health.agent.module.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体
 */
@Data
public class Document {
    private Long id;
    private Long knowledgeBaseId;
    private String filename;
    private String content;
    private String chunks;
    private String vectorIds;
    private String status;
    private LocalDateTime uploadedAt;
}

