package com.finorchestrator.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractExpiringEvent {
    private Long contractId;
    private Long userId;
    private String title;
    private String counterparty;
    private LocalDate expirationDate;
    private LocalDate renewalDate;
    private long daysRemaining;
}
