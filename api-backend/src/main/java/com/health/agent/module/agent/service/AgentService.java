package com.health.agent.module.agent.service;

import com.health.agent.common.api.PageResponse;
import com.health.agent.module.agent.dto.AgentCreateRequest;
import com.health.agent.module.agent.dto.AgentListQuery;
import com.health.agent.module.agent.dto.AgentTestRequest;
import com.health.agent.module.agent.dto.AgentUpdateRequest;
import com.health.agent.module.agent.vo.AgentTestResponse;
import com.health.agent.module.agent.vo.AgentVO;

/**
 * 智能体服务
 */
public interface AgentService {

    Long createAgent(AgentCreateRequest request);

    void updateAgent(Long id, AgentUpdateRequest request);

    AgentVO getAgent(Long id);

    PageResponse<AgentVO> listAgents(AgentListQuery query);

    void publishAgent(Long id);

    AgentTestResponse testAgent(Long id, AgentTestRequest request);
}

