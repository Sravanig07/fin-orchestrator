package com.finorchestrator.finance.service;

import com.finorchestrator.common.dto.PageResponse;
import com.finorchestrator.finance.dto.CreateTransactionRequest;
import com.finorchestrator.finance.dto.TransactionDto;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    PageResponse<TransactionDto> getTransactions(Long userId, int page, int size);
    List<TransactionDto> searchTransactions(Long userId, String query);
    List<TransactionDto> getTransactionsBetween(Long userId, LocalDate from, LocalDate to);
    TransactionDto createTransaction(Long userId, CreateTransactionRequest request);
}
