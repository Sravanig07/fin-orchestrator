package com.finorchestrator.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private String icon;
    private String color;
    private BigDecimal budgetLimit;
}
