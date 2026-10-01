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
public class SpendingPatternDto {
    private Long userId;
    private BigDecimal currentMonthSpending;
    private BigDecimal averagePreviousMonthlySpending;
    private BigDecimal percentageChange;
    private boolean anomalyDetected;
    private List<CategoryAnomaly> anomalies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryAnomaly {
        private String categoryName;
        private BigDecimal currentSpending;
        private BigDecimal historicalBaseline;
        private BigDecimal deviationPercentage;
        private boolean isHighVariance;
    }
}
