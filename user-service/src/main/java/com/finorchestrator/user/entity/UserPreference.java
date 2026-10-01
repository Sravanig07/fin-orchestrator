package com.finorchestrator.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "monthly_budget", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal monthlyBudget = new BigDecimal("80000.00");

    @Column(name = "risk_tolerance", nullable = false, length = 30)
    @Builder.Default
    private String riskTolerance = "MODERATE";

    @Column(name = "savings_target_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal savingsTargetPercentage = new BigDecimal("20.00");

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
