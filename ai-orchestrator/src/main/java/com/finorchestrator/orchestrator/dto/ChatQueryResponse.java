package com.finorchestrator.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatQueryResponse {
    private String answer;
    private String intent;
    private boolean isDeterministic;
    private String executionPlanReasoning;
    private List<ToolCallSummary> toolsUsed;
    private List<String> citations;
    private String executionTraceId;
    private String correlationId;
    private Long durationMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallSummary {
        private String serverCategory;
        private String toolName;
        private Map<String, Object> arguments;
        private Object result;
        private boolean success;
        private Long durationMs;
    }
}
