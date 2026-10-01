package com.finorchestrator.audit.controller;

import com.finorchestrator.audit.dto.AuditTraceDto;
import com.finorchestrator.audit.service.AuditService;
import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/traces")
    public ResponseEntity<ApiResponse<AuditTraceDto>> recordTrace(@RequestBody AuditTraceDto dto) {
        AuditTraceDto saved = auditService.recordTrace(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Trace recorded successfully", saved));
    }

    @GetMapping("/traces")
    public ResponseEntity<ApiResponse<List<AuditTraceDto>>> getTraces(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<AuditTraceDto> traces = auditService.getTracesForUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(traces));
    }

    @GetMapping("/traces/{traceId}")
    public ResponseEntity<ApiResponse<AuditTraceDto>> getTrace(@PathVariable("traceId") String traceId) {
        AuditTraceDto trace = auditService.getTrace(traceId);
        return ResponseEntity.ok(ApiResponse.ok(trace));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
