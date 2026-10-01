package com.finorchestrator.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {

    @NotBlank(message = "Query text is required")
    private String query;

    private Long contractId;
    private String documentId;

    @Builder.Default
    private int topK = 5;

    @Builder.Default
    private double minScore = 0.40;
}
