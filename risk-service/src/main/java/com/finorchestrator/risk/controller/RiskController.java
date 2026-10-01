package com.finorchestrator.risk.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.risk.dto.*;
import com.finorchestrator.risk.service.RiskEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskEngineService riskEngineService;

    @GetMapping("/cash-buffer")
    public ResponseEntity<ApiResponse<CashBufferDto>> getCashBuffer(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        CashBufferDto dto = riskEngineService.calculateCashBuffer(userId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/signals")
    public ResponseEntity<ApiResponse<List<RiskSignalDto>>> getSignals(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<RiskSignalDto> signals = riskEngineService.detectRiskSignals(userId);
        return ResponseEntity.ok(ApiResponse.ok(signals));
    }

    @GetMapping("/spending-pattern")
    public ResponseEntity<ApiResponse<SpendingPatternDto>> getSpendingPattern(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        SpendingPatternDto pattern = riskEngineService.analyzeSpendingPattern(userId);
        return ResponseEntity.ok(ApiResponse.ok(pattern));
    }

    @GetMapping("/obligations")
    public ResponseEntity<ApiResponse<FinancialObligationsDto>> getObligations(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        FinancialObligationsDto obligations = riskEngineService.analyzeFinancialObligations(userId);
        return ResponseEntity.ok(ApiResponse.ok(obligations));
    }

    @PostMapping("/analyze-transaction")
    public ResponseEntity<ApiResponse<TransactionRiskAnalysisDto>> analyzeTransaction(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestBody Map<String, Object> request) {
        Long userId = resolveUserId(headerUserId);
        Long txId = request.get("transactionId") != null ? Long.parseLong(request.get("transactionId").toString()) : null;
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        TransactionRiskAnalysisDto analysis = riskEngineService.analyzeTransaction(userId, txId, amount);
        return ResponseEntity.ok(ApiResponse.ok(analysis));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
