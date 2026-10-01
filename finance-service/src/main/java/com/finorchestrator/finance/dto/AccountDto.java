package com.finorchestrator.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private Long userId;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String status;
    private Instant createdAt;
}
