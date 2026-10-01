package com.finorchestrator.audit.service;

import com.finorchestrator.audit.dto.AuditTraceDto;

import java.util.List;

public interface AuditService {
    AuditTraceDto recordTrace(AuditTraceDto traceDto);
    List<AuditTraceDto> getTracesForUser(Long userId);
    AuditTraceDto getTrace(String traceId);
}
