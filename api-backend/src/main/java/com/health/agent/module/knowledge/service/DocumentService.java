package com.health.agent.module.knowledge.service;

import com.health.agent.module.knowledge.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文档服务
 */
public interface DocumentService {

    DocumentVO uploadDocument(Long knowledgeBaseId, MultipartFile file) throws IOException;

    List<DocumentVO> listDocuments(Long knowledgeBaseId, String status);
}

