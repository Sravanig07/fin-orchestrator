package com.finorchestrator.audit.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.audit.dto.AuditTraceDto;
import com.finorchestrator.audit.entity.AuditTrace;
import com.finorchestrator.audit.repository.AuditTraceRepository;
import com.finorchestrator.audit.service.AuditService;
import com.finorchestrator.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditTraceRepository auditTraceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AuditTraceDto recordTrace(AuditTraceDto dto) {
        String traceId = dto.getTraceId() != null ? dto.getTraceId() : "TRACE-" + UUID.randomUUID().toString();
        log.info("Recording execution trace: {} for userId: {}", traceId, dto.getUserId());

        String planJson = "[]";
        String toolsJson = "[]";

        try {
            if (dto.getPlanSteps() != null) planJson = objectMapper.writeValueAsString(dto.getPlanSteps());
            if (dto.getToolsCalled() != null) toolsJson = objectMapper.writeValueAsString(dto.getToolsCalled());
        } catch (Exception e) {
            log.warn("Failed to serialize audit json fields: {}", e.getMessage());
        }

        AuditTrace trace = AuditTrace.builder()
                .traceId(traceId)
                .correlationId(dto.getCorrelationId() != null ? dto.getCorrelationId() : "NO-CORRELATION")
                .userId(dto.getUserId())
                .userQuery(dto.getUserQuery())
                .intent(dto.getIntent())
                .planStepsJson(planJson)
                .toolsCalledJson(toolsJson)
                .validationStatus(dto.getValidationStatus() != null ? dto.getValidationStatus() : "VALIDATED")
                .finalResponse(dto.getFinalResponse())
                .durationMs(dto.getDurationMs() != null ? dto.getDurationMs() : 0L)
                .tokensUsed(dto.getTokensUsed() != null ? dto.getTokensUsed() : 0)
                .build();

        trace = auditTraceRepository.save(trace);
        return mapToDto(trace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditTraceDto> getTracesForUser(Long userId) {
        return auditTraceRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditTraceDto getTrace(String traceId) {
        AuditTrace trace = auditTraceRepository.findByTraceId(traceId)
                .orElseThrow(() -> new ResourceNotFoundException("AuditTrace", traceId));
        return mapToDto(trace);
    }

    private AuditTraceDto mapToDto(AuditTrace t) {
        List<String> planSteps = Collections.emptyList();
        List<Map<String, Object>> tools = Collections.emptyList();

        try {
            if (t.getPlanStepsJson() != null) {
                planSteps = objectMapper.readValue(t.getPlanStepsJson(), new TypeReference<>() {});
            }
            if (t.getToolsCalledJson() != null) {
                tools = objectMapper.readValue(t.getToolsCalledJson(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {}

        return AuditTraceDto.builder()
                .traceId(t.getTraceId())
                .correlationId(t.getCorrelationId())
                .userId(t.getUserId())
                .userQuery(t.getUserQuery())
                .intent(t.getIntent())
                .planSteps(planSteps)
                .toolsCalled(tools)
                .validationStatus(t.getValidationStatus())
                .finalResponse(t.getFinalResponse())
                .durationMs(t.getDurationMs())
                .tokensUsed(t.getTokensUsed())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
