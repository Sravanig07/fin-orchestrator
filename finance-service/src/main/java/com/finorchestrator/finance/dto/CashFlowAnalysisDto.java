package com.finorchestrator.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowAnalysisDto {
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal netCashFlow;
    private BigDecimal averageDailyBurnRate;
    private BigDecimal projectedMonthEndBalance;
    private int daysElapsed;
    private int daysRemaining;
    private List<DailyCashFlowPoint> trend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCashFlowPoint {
        private LocalDate date;
        private BigDecimal inflow;
        private BigDecimal outflow;
        private BigDecimal net;
    }
}
