package com.finorchestrator.finance.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.dto.PageResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.finance.dto.CreateTransactionRequest;
import com.finorchestrator.finance.dto.TransactionDto;
import com.finorchestrator.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionDto>>> getTransactions(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long userId = resolveUserId(headerUserId);
        PageResponse<TransactionDto> transactions = transactionService.getTransactions(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> searchTransactions(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam("q") String query) {
        Long userId = resolveUserId(headerUserId);
        List<TransactionDto> results = transactionService.searchTransactions(userId, query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsRange(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = resolveUserId(headerUserId);
        List<TransactionDto> results = transactionService.getTransactionsBetween(userId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDto>> createTransaction(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody CreateTransactionRequest request) {
        Long userId = resolveUserId(headerUserId);
        TransactionDto created = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transaction recorded successfully", created));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
