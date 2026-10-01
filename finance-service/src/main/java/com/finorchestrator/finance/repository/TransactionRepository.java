package com.finorchestrator.finance.repository;

import com.finorchestrator.finance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.transactionDate BETWEEN :from AND :to")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT t.categoryId as categoryId, COALESCE(SUM(t.amount), 0) as totalAmount " +
           "FROM Transaction t WHERE t.userId = :userId AND t.type = 'DEBIT' AND t.transactionDate BETWEEN :from AND :to " +
           "GROUP BY t.categoryId")
    List<Object[]> sumExpensesByCategory(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND " +
           "(LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.merchant) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Transaction> searchTransactions(@Param("userId") Long userId, @Param("query") String query);
}
