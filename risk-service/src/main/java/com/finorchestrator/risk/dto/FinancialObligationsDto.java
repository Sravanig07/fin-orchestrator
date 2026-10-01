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
public class FinancialObligationsDto {
    private Long userId;
    private BigDecimal monthlyFixedObligations;
    private BigDecimal monthlyNetIncome;
    private BigDecimal obligationToIncomeRatio; // (obligations / income) * 100
    private String riskLevel; // SAFE (<35%), MODERATE (35-50%), HIGH (>50%)
    private List<ObligationItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObligationItem {
        private String name;
        private BigDecimal monthlyAmount;
        private String type; // LOAN_EMI, SUBSCRIPTION, UTILITY, INSURANCE
    }
}
