package com.finorchestrator.finance.repository;

import com.finorchestrator.finance.entity.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {
    List<RecurringPayment> findByUserId(Long userId);
    List<RecurringPayment> findByUserIdAndStatus(Long userId, String status);
    List<RecurringPayment> findByUserIdAndNextDueDateBetweenAndStatus(Long userId, LocalDate from, LocalDate to, String status);
}
