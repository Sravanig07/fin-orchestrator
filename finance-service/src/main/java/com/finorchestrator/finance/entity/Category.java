package com.finorchestrator.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; // NULL for system default

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String type; // INCOME, EXPENSE

    @Column(length = 50)
    @Builder.Default
    private String icon = "tag";

    @Column(length = 20)
    @Builder.Default
    private String color = "#4F46E5";

    @Column(name = "budget_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
