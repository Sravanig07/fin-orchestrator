package com.finorchestrator.finance.service.impl;

import com.finorchestrator.common.dto.PageResponse;
import com.finorchestrator.common.event.DomainEvent;
import com.finorchestrator.common.event.TransactionCreatedEvent;
import com.finorchestrator.common.exception.ApiException;
import com.finorchestrator.common.exception.ResourceNotFoundException;
import com.finorchestrator.finance.dto.CreateTransactionRequest;
import com.finorchestrator.finance.dto.TransactionDto;
import com.finorchestrator.finance.entity.Account;
import com.finorchestrator.finance.entity.Category;
import com.finorchestrator.finance.entity.Transaction;
import com.finorchestrator.finance.repository.AccountRepository;
import com.finorchestrator.finance.repository.CategoryRepository;
import com.finorchestrator.finance.repository.TransactionRepository;
import com.finorchestrator.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private static final String TRANSACTIONS_TOPIC = "finorchestrator.transactions.v1";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> getTransactions(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("transactionDate").descending().and(Sort.by("id").descending()));
        Page<Transaction> pageResult = transactionRepository.findByUserId(userId, pageRequest);

        Map<Long, String> accountNames = getAccountNameMap(userId);
        Map<Long, String> categoryNames = getCategoryNameMap(userId);

        List<TransactionDto> dtos = pageResult.getContent().stream()
                .map(t -> mapToDto(t, accountNames, categoryNames))
                .collect(Collectors.toList());

        return PageResponse.<TransactionDto>builder()
                .content(dtos)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> searchTransactions(Long userId, String query) {
        Map<Long, String> accountNames = getAccountNameMap(userId);
        Map<Long, String> categoryNames = getCategoryNameMap(userId);

        return transactionRepository.searchTransactions(userId, query).stream()
                .map(t -> mapToDto(t, accountNames, categoryNames))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsBetween(Long userId, LocalDate from, LocalDate to) {
        Map<Long, String> accountNames = getAccountNameMap(userId);
        Map<Long, String> categoryNames = getCategoryNameMap(userId);

        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, from, to).stream()
                .map(t -> mapToDto(t, accountNames, categoryNames))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        // 1. Idempotency Check
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent transaction duplicate detected with key: {}", request.getIdempotencyKey());
                return mapToDto(existing.get(), getAccountNameMap(userId), getCategoryNameMap(userId));
            }
        }

        // 2. Account Validation & Balance Update with Optimistic Locking
        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId()));

        boolean isDebit = "DEBIT".equalsIgnoreCase(request.getType());
        BigDecimal newBalance = isDebit
                ? account.getBalance().subtract(request.getAmount())
                : account.getBalance().add(request.getAmount());

        account.setBalance(newBalance);
        accountRepository.save(account);

        // 3. Category Validation
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        LocalDate txDate = request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now();

        // 4. Save Transaction
        Transaction tx = Transaction.builder()
                .userId(userId)
                .accountId(account.getId())
                .categoryId(category.getId())
                .amount(request.getAmount())
                .type(isDebit ? "DEBIT" : "CREDIT")
                .description(request.getDescription())
                .merchant(request.getMerchant())
                .transactionDate(txDate)
                .status("COMPLETED")
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        tx = transactionRepository.save(tx);
        log.info("Saved transaction id: {} for user: {}, amount: {}", tx.getId(), userId, tx.getAmount());

        // 5. Publish Kafka Event
        publishTransactionEvent(tx);

        return mapToDto(tx, Map.of(account.getId(), account.getAccountName()), Map.of(category.getId(), category.getName()));
    }

    private void publishTransactionEvent(Transaction tx) {
        try {
            TransactionCreatedEvent eventPayload = TransactionCreatedEvent.builder()
                    .transactionId(tx.getId())
                    .userId(tx.getUserId())
                    .accountId(tx.getAccountId())
                    .categoryId(tx.getCategoryId())
                    .amount(tx.getAmount())
                    .type(tx.getType())
                    .description(tx.getDescription())
                    .merchant(tx.getMerchant())
                    .transactionDate(tx.getTransactionDate())
                    .build();

            DomainEvent<TransactionCreatedEvent> event = DomainEvent.of("TRANSACTION_CREATED", "finance-service", eventPayload);
            kafkaTemplate.send(TRANSACTIONS_TOPIC, String.valueOf(tx.getUserId()), event);
            log.debug("Emitted TRANSACTION_CREATED event to Kafka for txId: {}", tx.getId());
        } catch (Exception e) {
            log.warn("Kafka emission for transaction failed gracefully: {}", e.getMessage());
        }
    }

    private Map<Long, String> getAccountNameMap(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Account::getId, Account::getAccountName, (a, b) -> a));
    }

    private Map<Long, String> getCategoryNameMap(Long userId) {
        return categoryRepository.findByUserIdOrUserIdIsNull(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private TransactionDto mapToDto(Transaction t, Map<Long, String> accounts, Map<Long, String> categories) {
        return TransactionDto.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .accountId(t.getAccountId())
                .accountName(accounts.getOrDefault(t.getAccountId(), "Account #" + t.getAccountId()))
                .categoryId(t.getCategoryId())
                .categoryName(categories.getOrDefault(t.getCategoryId(), "General"))
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .merchant(t.getMerchant())
                .transactionDate(t.getTransactionDate())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
