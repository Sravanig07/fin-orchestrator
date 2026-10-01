package com.finorchestrator.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRiskAnalysisDto {
    private Long transactionId;
    private Long userId;
    private BigDecimal amount;
    private boolean isRiskFlagged;
    private String riskSeverity; // LOW, MEDIUM, HIGH, NONE
    private List<String> triggerReasons;
    private BigDecimal postTransactionEstimatedBufferMonths;
}
