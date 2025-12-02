package com.health.agent.module.knowledge.service;

import com.health.agent.module.knowledge.dto.KnowledgeBaseCreateRequest;
import com.health.agent.module.knowledge.vo.KnowledgeBaseVO;

import java.util.List;

/**
 * 知识库服务
 */
public interface KnowledgeBaseService {

    Long createKnowledgeBase(KnowledgeBaseCreateRequest request);

    List<KnowledgeBaseVO> listKnowledgeBases();

    KnowledgeBaseVO getKnowledgeBase(Long id);
}

