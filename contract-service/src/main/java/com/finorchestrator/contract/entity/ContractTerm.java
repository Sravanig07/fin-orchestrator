package com.finorchestrator.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "contract_terms", indexes = {
        @Index(name = "idx_terms_contract", columnList = "contract_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "term_key", nullable = false, length = 100)
    private String termKey; // INTEREST_RATE, PRINCIPAL, PREMIUM, GRACE_PERIOD, EARLY_REPAYMENT_FEE, LATE_PENALTY

    @Column(name = "term_value", nullable = false)
    private String termValue;

    @Column(length = 50)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal confidenceScore = new BigDecimal("1.00");

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
