package com.finorchestrator.orchestrator.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanStep {
    private int stepNumber;
    private String serverCategory; // finance, contract, risk, notification
    private String toolName;
    private String description;
    private Map<String, Object> arguments;
    private Object result;
    @Builder.Default
    private boolean executed = false;
    @Builder.Default
    private boolean success = false;
    private String errorMessage;
}
