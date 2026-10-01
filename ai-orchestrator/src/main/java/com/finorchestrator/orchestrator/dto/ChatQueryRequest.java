package com.finorchestrator.orchestrator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatQueryRequest {

    @NotBlank(message = "Query is required")
    private String query;

    private String sessionId;

    private Map<String, Object> context;
}
