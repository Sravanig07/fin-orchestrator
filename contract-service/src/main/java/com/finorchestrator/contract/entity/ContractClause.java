package com.finorchestrator.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "contract_clauses", indexes = {
        @Index(name = "idx_clauses_contract", columnList = "contract_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "clause_title", nullable = false, length = 200)
    private String clauseTitle;

    @Column(name = "section_number", length = 50)
    private String sectionNumber;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "chunk_id", nullable = false, length = 100)
    private String chunkId;

    @Column(name = "clause_type", nullable = false, length = 50)
    private String clauseType; // PENALTY, FEE, OBLIGATION, TERMINATION, RENEWAL

    @Column(name = "clause_text", nullable = false, columnDefinition = "TEXT")
    private String clauseText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
