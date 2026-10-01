package com.finorchestrator.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskSignalDto {
    private Long id;
    private Long userId;
    private String riskType;
    private String severity;
    private String description;
    private Map<String, Object> metrics;
    private String status;
    private Instant createdAt;
}
