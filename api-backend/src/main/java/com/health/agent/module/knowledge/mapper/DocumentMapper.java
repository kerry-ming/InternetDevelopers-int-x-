package com.health.agent.module.knowledge.mapper;

import com.health.agent.module.knowledge.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档Mapper
 */
@Mapper
public interface DocumentMapper {

    int insert(Document document);

    int update(Document document);

    Document selectById(@Param("id") Long id);

    List<Document> selectByKnowledgeBase(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                         @Param("status") String status);
}

