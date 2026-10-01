package com.finorchestrator.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentChunk {
    private String chunkId;
    private String documentId;
    private Long contractId;
    private String title;
    private int pageNumber;
    private String sectionNumber;
    private String clauseType; // PENALTY, FEE, OBLIGATION, TERMINATION, RENEWAL, GENERAL
    private String text;
    private List<Float> embedding;
    private Double score;
}
