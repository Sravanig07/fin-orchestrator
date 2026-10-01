package com.finorchestrator.finance.service;

import com.finorchestrator.finance.dto.AccountDto;
import com.finorchestrator.finance.dto.CreateAccountRequest;

import java.util.List;

public interface AccountService {
    List<AccountDto> getAccounts(Long userId);
    AccountDto getAccount(Long id, Long userId);
    AccountDto createAccount(Long userId, CreateAccountRequest request);
}
