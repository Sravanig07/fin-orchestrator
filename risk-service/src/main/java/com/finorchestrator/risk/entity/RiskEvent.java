package com.finorchestrator.risk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "risk_events", indexes = {
        @Index(name = "idx_risk_user_status", columnList = "user_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "risk_type", nullable = false, length = 100)
    private String riskType; // LIQUIDITY_BUFFER_LOW, SPENDING_SPIKE, DEBT_BURDEN_HIGH, UPCOMING_PAYMENT_RISK

    @Column(nullable = false, length = 30)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, RESOLVED, ACKNOWLEDGED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
