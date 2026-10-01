package com.finorchestrator.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTermDto {
    private Long id;
    private Long contractId;
    private String termKey;
    private String termValue;
    private String unit;
    private String description;
    private BigDecimal confidenceScore;
}
