package com.finorchestrator.risk.repository;

import com.finorchestrator.risk.entity.RiskEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {
    List<RiskEvent> findByUserId(Long userId);
    List<RiskEvent> findByUserIdAndStatus(Long userId, String status);
}
