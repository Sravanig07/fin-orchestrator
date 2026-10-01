package com.finorchestrator.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractClauseDto {
    private Long id;
    private Long contractId;
    private String clauseTitle;
    private String sectionNumber;
    private Integer pageNumber;
    private String chunkId;
    private String clauseType; // PENALTY, FEE, OBLIGATION, TERMINATION, RENEWAL
    private String clauseText;
}
