package com.health.agent.module.knowledge.service.impl;

import com.health.agent.common.exception.BusinessException;
import com.health.agent.module.knowledge.dto.KnowledgeBaseCreateRequest;
import com.health.agent.module.knowledge.entity.KnowledgeBase;
import com.health.agent.module.knowledge.mapper.KnowledgeBaseMapper;
import com.health.agent.module.knowledge.service.KnowledgeBaseService;
import com.health.agent.module.knowledge.vo.KnowledgeBaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public Long createKnowledgeBase(KnowledgeBaseCreateRequest request) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(request.getName());
        knowledgeBase.setDescription(request.getDescription());
        knowledgeBase.setVectorDbType(request.getVectorDbType());
        knowledgeBase.setChunkSize(request.getChunkSize());
        knowledgeBase.setChunkOverlap(request.getChunkOverlap());
        knowledgeBaseMapper.insert(knowledgeBase);
        return knowledgeBase.getId();
    }

    @Override
    public List<KnowledgeBaseVO> listKnowledgeBases() {
        return knowledgeBaseMapper.selectAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeBaseVO getKnowledgeBase(Long id) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(id);
        if (knowledgeBase == null) {
            throw new BusinessException("知识库不存在");
        }
        return convertToVO(knowledgeBase);
    }

    private KnowledgeBaseVO convertToVO(KnowledgeBase knowledgeBase) {
        return KnowledgeBaseVO.builder()
                .id(knowledgeBase.getId())
                .name(knowledgeBase.getName())
                .description(knowledgeBase.getDescription())
                .vectorDbType(knowledgeBase.getVectorDbType())
                .chunkSize(knowledgeBase.getChunkSize())
                .chunkOverlap(knowledgeBase.getChunkOverlap())
                .createdAt(knowledgeBase.getCreatedAt())
                .updatedAt(knowledgeBase.getUpdatedAt())
                .build();
    }
}

