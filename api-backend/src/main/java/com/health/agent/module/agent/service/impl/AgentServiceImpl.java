package com.health.agent.module.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.agent.common.api.PageResponse;
import com.health.agent.common.exception.BusinessException;
import com.health.agent.module.agent.dto.AgentCreateRequest;
import com.health.agent.module.agent.dto.AgentListQuery;
import com.health.agent.module.agent.dto.AgentTestRequest;
import com.health.agent.module.agent.dto.AgentUpdateRequest;
import com.health.agent.module.agent.dto.ModelConfigRequest;
import com.health.agent.module.agent.entity.Agent;
import com.health.agent.module.agent.mapper.AgentMapper;
import com.health.agent.module.agent.model.ModelConfig;
import com.health.agent.module.agent.service.AgentService;
import com.health.agent.module.agent.vo.AgentTestResponse;
import com.health.agent.module.agent.vo.AgentVO;
import com.health.agent.module.knowledge.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体服务实现
 */
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {};

    private final AgentMapper agentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAgent(AgentCreateRequest request) {
        validateModelConfig(request.getModelConfig());
        validateKnowledgeBaseIds(request.getKnowledgeBaseIds());

        Agent agent = new Agent();
        agent.setName(request.getName());
        agent.setDescription(request.getDescription());
        agent.setSystemPrompt(request.getSystemPrompt());
        agent.setUserPromptTemplate(request.getUserPromptTemplate());
        agent.setModelConfig(writeModelConfig(request.getModelConfig()));
        agent.setWorkflowId(request.getWorkflowId());
        agent.setKnowledgeBaseIds(writeIds(request.getKnowledgeBaseIds()));
        agent.setPluginIds(writeIds(request.getPluginIds()));
        agent.setStatus("draft");

        agentMapper.insert(agent);
        return agent.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgent(Long id, AgentUpdateRequest request) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BusinessException("智能体不存在");
        }

        if (request.getName() != null) {
            agent.setName(request.getName());
        }
        if (request.getDescription() != null) {
            agent.setDescription(request.getDescription());
        }
        if (request.getSystemPrompt() != null) {
            agent.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getUserPromptTemplate() != null) {
            agent.setUserPromptTemplate(request.getUserPromptTemplate());
        }
        if (request.getModelConfig() != null) {
            validateModelConfig(request.getModelConfig());
            agent.setModelConfig(writeModelConfig(request.getModelConfig()));
        }
        if (request.getWorkflowId() != null) {
            agent.setWorkflowId(request.getWorkflowId());
        }
        if (request.getKnowledgeBaseIds() != null) {
            validateKnowledgeBaseIds(request.getKnowledgeBaseIds());
            agent.setKnowledgeBaseIds(writeIds(request.getKnowledgeBaseIds()));
        }
        if (request.getPluginIds() != null) {
            agent.setPluginIds(writeIds(request.getPluginIds()));
        }

        agentMapper.update(agent);
    }

    @Override
    public AgentVO getAgent(Long id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BusinessException("智能体不存在");
        }
        return convertToVO(agent);
    }

    @Override
    public PageResponse<AgentVO> listAgents(AgentListQuery query) {
        int pageNo = query.getPageNo() != null && query.getPageNo() > 0 ? query.getPageNo() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 20;
        int offset = (pageNo - 1) * pageSize;

        long total = agentMapper.countByCondition(query.getKeyword(), query.getStatus());
        if (total == 0) {
            return PageResponse.empty(pageNo, pageSize);
        }

        List<Agent> agents = agentMapper.selectPageByCondition(query.getKeyword(), query.getStatus(), pageSize, offset);
        List<AgentVO> vos = agents.stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResponse.of(total, pageNo, pageSize, vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAgent(Long id) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BusinessException("智能体不存在");
        }
        if (!StringUtils.hasText(agent.getName())) {
            throw new BusinessException("请填写智能体名称");
        }
        if (!StringUtils.hasText(agent.getSystemPrompt())) {
            throw new BusinessException("请配置系统提示词");
        }
        if (!StringUtils.hasText(agent.getModelConfig())) {
            throw new BusinessException("请配置模型参数");
        }

        List<Long> kbIds = readIds(agent.getKnowledgeBaseIds());
        if (!CollectionUtils.isEmpty(kbIds)) {
            validateKnowledgeBaseIds(kbIds);
        }

        agent.setStatus("published");
        agentMapper.updateStatus(agent);
    }

    @Override
    public AgentTestResponse testAgent(Long id, AgentTestRequest request) {
        Agent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new BusinessException("智能体不存在");
        }
        long start = Instant.now().toEpochMilli();
        String reply = "[Mock Reply] " + request.getQuestion();
        long elapsed = Instant.now().toEpochMilli() - start;
        return AgentTestResponse.builder()
                .reply(reply)
                .elapsedMs(elapsed)
                .promptTokens(request.getQuestion().length() / 4 + 1)
                .completionTokens(reply.length() / 4 + 1)
                .build();
    }

    private void validateModelConfig(ModelConfigRequest modelConfig) {
        if (modelConfig == null) {
            throw new BusinessException("模型配置不能为空");
        }
        if (!StringUtils.hasText(modelConfig.getProvider())) {
            throw new BusinessException("模型服务商不能为空");
        }
        if (!StringUtils.hasText(modelConfig.getModel())) {
            throw new BusinessException("模型名称不能为空");
        }
    }

    private void validateKnowledgeBaseIds(List<Long> knowledgeBaseIds) {
        if (CollectionUtils.isEmpty(knowledgeBaseIds)) {
            return;
        }
        long count = knowledgeBaseMapper.countByIds(knowledgeBaseIds);
        if (count != knowledgeBaseIds.size()) {
            throw new BusinessException("存在无效的知识库ID");
        }
    }

    private String writeModelConfig(ModelConfigRequest modelConfigRequest) {
        ModelConfig config = new ModelConfig();
        config.setProvider(modelConfigRequest.getProvider());
        config.setModel(modelConfigRequest.getModel());
        config.setTemperature(modelConfigRequest.getTemperature());
        config.setMaxTokens(modelConfigRequest.getMaxTokens());
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new BusinessException("模型配置序列化失败");
        }
    }

    private String writeIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            throw new BusinessException("ID列表序列化失败");
        }
    }

    private List<Long> readIds(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, LONG_LIST_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private ModelConfig readModelConfig(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ModelConfig.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private AgentVO convertToVO(Agent agent) {
        return AgentVO.builder()
                .id(agent.getId())
                .name(agent.getName())
                .description(agent.getDescription())
                .systemPrompt(agent.getSystemPrompt())
                .userPromptTemplate(agent.getUserPromptTemplate())
                .modelConfig(readModelConfig(agent.getModelConfig()))
                .workflowId(agent.getWorkflowId())
                .knowledgeBaseIds(readIds(agent.getKnowledgeBaseIds()))
                .pluginIds(readIds(agent.getPluginIds()))
                .status(agent.getStatus())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }
}

