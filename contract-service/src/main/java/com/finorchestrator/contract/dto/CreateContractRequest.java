package com.finorchestrator.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContractRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Contract type is required (LOAN, INSURANCE, CREDIT_CARD, LEASE, SUBSCRIPTION)")
    private String contractType;

    @NotBlank(message = "Counterparty is required")
    private String counterparty;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private LocalDate renewalDate;
    private String documentId;
    private String filePath;

    private List<ContractTermDto> terms;
    private List<ContractClauseDto> clauses;
}
