package com.finorchestrator.finance.service;

import com.finorchestrator.finance.dto.CashFlowAnalysisDto;
import com.finorchestrator.finance.dto.FinancialSummaryDto;
import com.finorchestrator.finance.dto.RecurringPaymentDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialAnalyticsService {
    FinancialSummaryDto getFinancialSummary(Long userId);
    CashFlowAnalysisDto getCashFlowAnalysis(Long userId, LocalDate start, LocalDate end);
    BigDecimal getMonthlyExpenses(Long userId, int year, int month);
    BigDecimal getMonthlyIncome(Long userId, int year, int month);
    List<RecurringPaymentDto> getRecurringPayments(Long userId);
}
