package com.finorchestrator.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashBufferDto {
    private Long userId;
    private BigDecimal totalLiquidFunds;
    private BigDecimal monthlyBurnRate;
    private BigDecimal bufferMonths; // liquid / burnRate
    private String runwayStatus; // CRITICAL (<1 mo), LOW (<3 mo), HEALTHY (3-6 mo), STRONG (>6 mo)
    private BigDecimal minimumRecommendedBuffer;
    private BigDecimal bufferDeficitOrSurplus;
}
