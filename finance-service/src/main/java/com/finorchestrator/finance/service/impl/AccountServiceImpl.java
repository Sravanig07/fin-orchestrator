package com.finorchestrator.finance.service.impl;

import com.finorchestrator.common.exception.ResourceNotFoundException;
import com.finorchestrator.finance.dto.AccountDto;
import com.finorchestrator.finance.dto.CreateAccountRequest;
import com.finorchestrator.finance.entity.Account;
import com.finorchestrator.finance.repository.AccountRepository;
import com.finorchestrator.finance.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountDto> getAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long id, Long userId) {
        Account account = accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return mapToDto(account);
    }

    @Override
    @Transactional
    public AccountDto createAccount(Long userId, CreateAccountRequest request) {
        Account account = Account.builder()
                .userId(userId)
                .accountName(request.getAccountName())
                .accountNumber(request.getAccountNumber())
                .accountType(request.getAccountType().toUpperCase())
                .balance(request.getInitialBalance())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status("ACTIVE")
                .version(0L)
                .build();

        account = accountRepository.save(account);
        return mapToDto(account);
    }

    private AccountDto mapToDto(Account a) {
        return AccountDto.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .accountName(a.getAccountName())
                .accountNumber(a.getAccountNumber())
                .accountType(a.getAccountType())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
