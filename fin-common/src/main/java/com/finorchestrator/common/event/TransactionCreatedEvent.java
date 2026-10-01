package com.finorchestrator.common.event;

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
public class TransactionCreatedEvent {
    private Long transactionId;
    private Long userId;
    private Long accountId;
    private Long categoryId;
    private BigDecimal amount;
    private String type; // DEBIT, CREDIT
    private String description;
    private String merchant;
    private LocalDate transactionDate;
}
