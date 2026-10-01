package com.finorchestrator.contract.repository;

import com.finorchestrator.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByUserId(Long userId);
    List<Contract> findByUserIdAndStatus(Long userId, String status);
    Optional<Contract> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Contract c WHERE c.userId = :userId AND " +
           "(c.endDate BETWEEN :from AND :to OR c.renewalDate BETWEEN :from AND :to)")
    List<Contract> findExpiringContracts(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT c FROM Contract c WHERE c.userId = :userId AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.counterparty) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Contract> searchContracts(@Param("userId") Long userId, @Param("query") String query);
}
