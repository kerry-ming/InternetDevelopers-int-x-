package com.health.agent.module.knowledge.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.agent.common.exception.BusinessException;
import com.health.agent.module.knowledge.entity.Document;
import com.health.agent.module.knowledge.entity.KnowledgeBase;
import com.health.agent.module.knowledge.enums.DocumentStatus;
import com.health.agent.module.knowledge.mapper.DocumentMapper;
import com.health.agent.module.knowledge.mapper.KnowledgeBaseMapper;
import com.health.agent.module.knowledge.service.DocumentService;
import com.health.agent.module.knowledge.vo.DocumentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_SUFFIX = List.of("txt", "md");
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO uploadDocument(Long knowledgeBaseId, MultipartFile file) throws IOException {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BusinessException("知识库不存在");
        }

        validateFile(file);

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("文件内容为空");
        }

        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFilename(file.getOriginalFilename());
        document.setContent(content);
        document.setStatus(DocumentStatus.toValue(DocumentStatus.PROCESSING));
        document.setUploadedAt(LocalDateTime.now());
        documentMapper.insert(document);

        try {
            processDocument(document, knowledgeBase);
        } catch (Exception ex) {
            log.error("文档处理失败", ex);
            document.setStatus(DocumentStatus.toValue(DocumentStatus.FAILED));
            documentMapper.update(document);
            throw new BusinessException("向量化处理失败");
        }

        document.setStatus(DocumentStatus.toValue(DocumentStatus.SUCCESS));
        documentMapper.update(document);
        return toVO(document);
    }

    @Override
    public List<DocumentVO> listDocuments(Long knowledgeBaseId, String status) {
        return documentMapper.selectByKnowledgeBase(knowledgeBaseId, status).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件过大，请上传小于10MB的文件");
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(ext) || !ALLOWED_SUFFIX.contains(ext.toLowerCase())) {
            throw new BusinessException("仅支持TXT和Markdown格式");
        }
    }

    private void processDocument(Document document, KnowledgeBase knowledgeBase) throws JsonProcessingException {
        int chunkSize = knowledgeBase.getChunkSize() != null ? knowledgeBase.getChunkSize() : 512;
        int chunkOverlap = knowledgeBase.getChunkOverlap() != null ? knowledgeBase.getChunkOverlap() : 50;
        List<String> chunks = chunkText(document.getContent(), chunkSize, chunkOverlap);
        document.setChunks(objectMapper.writeValueAsString(chunks));

        List<String> vectorIds = chunks.stream()
                .map(chunk -> "vec_" + document.getId() + "_" + UUID.randomUUID())
                .collect(Collectors.toList());
        document.setVectorIds(objectMapper.writeValueAsString(vectorIds));
    }

    private List<String> chunkText(String content, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return chunks;
        }
        int start = 0;
        int length = content.length();
        chunkOverlap = Math.max(0, Math.min(chunkOverlap, chunkSize - 1));
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(content.substring(start, end));
            if (end == length) {
                break;
            }
            start = end - chunkOverlap;
        }
        return chunks;
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private DocumentVO toVO(Document document) {
        return DocumentVO.builder()
                .id(document.getId())
                .filename(document.getFilename())
                .status(document.getStatus())
                .chunkCount(readChunkCount(document.getChunks()))
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    private Integer readChunkCount(String jsonChunk) {
        if (!StringUtils.hasText(jsonChunk)) {
            return 0;
        }
        try {
            List<String> chunks = objectMapper.readValue(jsonChunk, STRING_LIST_TYPE);
            return CollectionUtils.isEmpty(chunks) ? 0 : chunks.size();
        } catch (JsonProcessingException e) {
            return 0;
        }
    }
}

