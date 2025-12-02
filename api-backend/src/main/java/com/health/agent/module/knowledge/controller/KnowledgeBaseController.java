package com.health.agent.module.knowledge.controller;

import com.health.agent.common.api.ApiResponse;
import com.health.agent.module.knowledge.dto.KnowledgeBaseCreateRequest;
import com.health.agent.module.knowledge.service.DocumentService;
import com.health.agent.module.knowledge.service.KnowledgeBaseService;
import com.health.agent.module.knowledge.vo.DocumentVO;
import com.health.agent.module.knowledge.vo.KnowledgeBaseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/api/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentService documentService;

    @PostMapping
    public ApiResponse<Long> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseCreateRequest request) {
        Long id = knowledgeBaseService.createKnowledgeBase(request);
        return ApiResponse.ok("知识库创建成功", id);
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseVO>> listKnowledgeBases() {
        return ApiResponse.ok(knowledgeBaseService.listKnowledgeBases());
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseVO> getKnowledgeBase(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeBaseService.getKnowledgeBase(id));
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DocumentVO> uploadDocument(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.ok(documentService.uploadDocument(id, file));
    }

    @GetMapping("/{id}/documents")
    public ApiResponse<List<DocumentVO>> listDocuments(@PathVariable Long id,
                                                       @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.ok(documentService.listDocuments(id, status));
    }
}

