package com.finorchestrator.finance.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.exception.UnauthorizedException;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.finance.dto.AccountDto;
import com.finorchestrator.finance.dto.CreateAccountRequest;
import com.finorchestrator.finance.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDto>>> getAccounts(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<AccountDto> accounts = accountService.getAccounts(userId);
        return ResponseEntity.ok(ApiResponse.ok(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        AccountDto account = accountService.getAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(account));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody CreateAccountRequest request) {
        Long userId = resolveUserId(headerUserId);
        AccountDto account = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully", account));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        if (userId == null) {
            // Default to demo user 1 if not set in direct testing
            return 1L;
        }
        return userId;
    }
}
