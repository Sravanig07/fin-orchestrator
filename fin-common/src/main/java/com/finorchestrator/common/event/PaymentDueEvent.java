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
public class PaymentDueEvent {
    private Long userId;
    private Long recurringPaymentId;
    private String merchant;
    private BigDecimal amount;
    private LocalDate dueDate;
}
