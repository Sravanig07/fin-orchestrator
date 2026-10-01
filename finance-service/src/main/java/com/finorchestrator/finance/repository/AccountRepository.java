package com.finorchestrator.finance.repository;

import com.finorchestrator.finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
    List<Account> findByUserIdAndStatus(Long userId, String status);
    Optional<Account> findByIdAndUserId(Long id, Long userId);
}
