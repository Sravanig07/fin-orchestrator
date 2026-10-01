package com.finorchestrator.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTraceDto {
    private String traceId;
    private String correlationId;
    private Long userId;
    private String userQuery;
    private String intent;
    private List<String> planSteps;
    private List<Map<String, Object>> toolsCalled;
    private String validationStatus;
    private String finalResponse;
    private Long durationMs;
    private Integer tokensUsed;
    private Instant createdAt;
}
