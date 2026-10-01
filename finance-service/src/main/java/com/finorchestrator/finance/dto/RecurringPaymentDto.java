package com.finorchestrator.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPaymentDto {
    private Long id;
    private Long userId;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private String merchant;
    private BigDecimal amount;
    private String frequency;
    private LocalDate nextDueDate;
    private String status;
    private Boolean autoDebit;
}
