package com.finorchestrator.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;
    private Long userId;
    private String title;
    private String contractType; // LOAN, INSURANCE, CREDIT_CARD, LEASE, SUBSCRIPTION
    private String counterparty;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private String status;
    private Integer version;
    private String documentId;
    private String filePath;
    private List<ContractTermDto> terms;
    private List<ContractClauseDto> clauses;
    private Instant createdAt;
}
