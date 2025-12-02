package com.health.agent.module.knowledge.mapper;

import com.health.agent.module.knowledge.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库Mapper
 */
@Mapper
public interface KnowledgeBaseMapper {

    int insert(KnowledgeBase knowledgeBase);

    List<KnowledgeBase> selectAll();

    KnowledgeBase selectById(@Param("id") Long id);

    long countByIds(@Param("ids") List<Long> ids);
}

