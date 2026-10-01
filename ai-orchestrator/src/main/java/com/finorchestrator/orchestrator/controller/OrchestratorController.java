package com.finorchestrator.orchestrator.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.orchestrator.agent.FinancialAgentOrchestrator;
import com.finorchestrator.orchestrator.dto.ChatQueryRequest;
import com.finorchestrator.orchestrator.dto.ChatQueryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    private final FinancialAgentOrchestrator orchestrator;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatQueryResponse>> chat(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody ChatQueryRequest request) {

        Long userId = resolveUserId(headerUserId);
        ChatQueryResponse response = orchestrator.processQuery(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<ChatQueryResponse>> query(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody ChatQueryRequest request) {

        Long userId = resolveUserId(headerUserId);
        ChatQueryResponse response = orchestrator.processQuery(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
