package com.finorchestrator.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryDto {
    private Long userId;
    private BigDecimal totalLiquidBalance;
    private BigDecimal totalDebt;
    private BigDecimal netWorth;
    private BigDecimal currentMonthIncome;
    private BigDecimal currentMonthExpenses;
    private BigDecimal netSavings;
    private BigDecimal monthlyRecurringTotal;
    private BigDecimal savingsRatePercentage;
    private Map<String, BigDecimal> expensesByCategory;
    private Instant calculatedAt;
}
