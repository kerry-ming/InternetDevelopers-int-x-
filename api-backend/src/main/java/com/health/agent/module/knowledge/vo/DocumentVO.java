package com.health.agent.module.knowledge.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档视图对象
 */
@Data
@Builder
public class DocumentVO {
    private Long id;
    private String filename;
    private String status;
    private Integer chunkCount;
    private LocalDateTime uploadedAt;
}

