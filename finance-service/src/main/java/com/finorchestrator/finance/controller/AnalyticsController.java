package com.finorchestrator.finance.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.finance.dto.CashFlowAnalysisDto;
import com.finorchestrator.finance.dto.FinancialSummaryDto;
import com.finorchestrator.finance.dto.RecurringPaymentDto;
import com.finorchestrator.finance.service.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finance/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final FinancialAnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FinancialSummaryDto>> getSummary(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        FinancialSummaryDto summary = analyticsService.getFinancialSummary(userId);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ApiResponse<CashFlowAnalysisDto>> getCashFlow(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = resolveUserId(headerUserId);
        CashFlowAnalysisDto cashFlow = analyticsService.getCashFlowAnalysis(userId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(cashFlow));
    }

    @GetMapping("/recurring")
    public ResponseEntity<ApiResponse<List<RecurringPaymentDto>>> getRecurringPayments(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = resolveUserId(headerUserId);
        List<RecurringPaymentDto> recurring = analyticsService.getRecurringPayments(userId);
        return ResponseEntity.ok(ApiResponse.ok(recurring));
    }

    @GetMapping("/monthly-expenses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyExpenses(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        Long userId = resolveUserId(headerUserId);
        BigDecimal totalExpenses = analyticsService.getMonthlyExpenses(userId, year, month);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("year", year, "month", month, "totalExpenses", totalExpenses)));
    }

    @GetMapping("/monthly-income")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyIncome(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        Long userId = resolveUserId(headerUserId);
        BigDecimal totalIncome = analyticsService.getMonthlyIncome(userId, year, month);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("year", year, "month", month, "totalIncome", totalIncome)));
    }

    private Long resolveUserId(Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        return userId != null ? userId : 1L;
    }
}
