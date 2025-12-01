package com.health.agent.module.knowledge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建知识库请求
 */
@Data
public class KnowledgeBaseCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500字符")
    private String description;

    private String vectorDbType = "milvus";

    @Min(value = 100, message = "分块大小需不小于100")
    @Max(value = 2000, message = "分块大小不能超过2000")
    private Integer chunkSize = 512;

    @Min(value = 0, message = "分块重叠不能小于0")
    @Max(value = 500, message = "分块重叠不能超过500")
    private Integer chunkOverlap = 50;
}

