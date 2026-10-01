package com.finorchestrator.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Monthly budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly budget must be greater than 0")
    private BigDecimal monthlyBudget;

    @NotBlank(message = "Risk tolerance is required")
    private String riskTolerance;

    @NotNull(message = "Savings target is required")
    private BigDecimal savingsTargetPercentage;
}
