package com.finorchestrator.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDetectedEvent {
    private Long userId;
    private String riskType;
    private String severity;
    private String description;
    private Map<String, Object> metrics;
}
