package com.finorchestrator.audit.repository;

import com.finorchestrator.audit.entity.AuditTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditTraceRepository extends JpaRepository<AuditTrace, Long> {
    List<AuditTrace> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<AuditTrace> findByTraceId(String traceId);
    List<AuditTrace> findByCorrelationId(String correlationId);
}
