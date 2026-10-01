package com.finorchestrator.contract.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.contract.dto.*;
import com.finorchestrator.contract.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractDto>>> getContracts(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<ContractDto> contracts = contractService.getContracts(userId);
        return ResponseEntity.ok(ApiResponse.ok(contracts));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ContractDto>>> searchContracts(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam("q") String query) {
        Long userId = resolveUserId(headerUserId);
        List<ContractDto> results = contractService.searchContracts(userId, query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<ContractDto>>> getExpiringContracts(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam(value = "daysAhead", defaultValue = "60") int daysAhead) {
        Long userId = resolveUserId(headerUserId);
        List<ContractDto> expiring = contractService.getExpiringContracts(userId, daysAhead);
        return ResponseEntity.ok(ApiResponse.ok(expiring));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractDto>> getContract(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        ContractDto contract = contractService.getContract(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(contract));
    }

    @GetMapping("/{id}/terms")
    public ResponseEntity<ApiResponse<List<ContractTermDto>>> getTerms(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<ContractTermDto> terms = contractService.getContractTerms(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(terms));
    }

    @GetMapping("/{id}/clauses")
    public ResponseEntity<ApiResponse<List<ContractClauseDto>>> getClauses(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<ContractClauseDto> clauses = contractService.getContractClauses(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(clauses));
    }

    @GetMapping("/{id}/penalties")
    public ResponseEntity<ApiResponse<List<ContractClauseDto>>> getPenalties(
            @PathVariable("id") Long id,
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<ContractClauseDto> penalties = contractService.getContractPenalties(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(penalties));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContractDto>> createContract(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody CreateContractRequest request) {
        Long userId = resolveUserId(headerUserId);
        ContractDto contract = contractService.createContract(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Contract created successfully", contract));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
