package com.finorchestrator.finance.service;

import com.finorchestrator.finance.dto.CashFlowAnalysisDto;
import com.finorchestrator.finance.dto.FinancialSummaryDto;
import com.finorchestrator.finance.entity.Account;
import com.finorchestrator.finance.entity.Category;
import com.finorchestrator.finance.entity.RecurringPayment;
import com.finorchestrator.finance.entity.Transaction;
import com.finorchestrator.finance.repository.AccountRepository;
import com.finorchestrator.finance.repository.CategoryRepository;
import com.finorchestrator.finance.repository.RecurringPaymentRepository;
import com.finorchestrator.finance.repository.TransactionRepository;
import com.finorchestrator.finance.service.impl.FinancialAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAnalyticsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecurringPaymentRepository recurringPaymentRepository;

    @InjectMocks
    private FinancialAnalyticsServiceImpl analyticsService;

    private Long userId = 1L;

    @Test
    @DisplayName("Financial Summary should calculate exact BigDecimal net worth and savings rate deterministically")
    void testGetFinancialSummary_DeterministicMath() {
        // Accounts: HDFC Checking (1,65,000), ICICI Savings (4,00,000), HDFC Credit (-24,650)
        Account checking = Account.builder().id(1L).userId(userId).accountType("CHECKING").balance(new BigDecimal("165000.00")).build();
        Account savings = Account.builder().id(2L).userId(userId).accountType("SAVINGS").balance(new BigDecimal("400000.00")).build();
        Account creditCard = Account.builder().id(3L).userId(userId).accountType("CREDIT_CARD").balance(new BigDecimal("-24650.00")).build();

        when(accountRepository.findByUserId(userId)).thenReturn(Arrays.asList(checking, savings, creditCard));

        // Income: 1,85,000, Expenses: 49,649
        BigDecimal income = new BigDecimal("185000.00");
        BigDecimal expenses = new BigDecimal("49649.00");
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(eq(userId), eq("CREDIT"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(income);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(eq(userId), eq("DEBIT"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expenses);

        // Recurring Payments: 32,000 EMI + 1,199 Internet
        RecurringPayment emi = RecurringPayment.builder().amount(new BigDecimal("32000.00")).frequency("MONTHLY").status("ACTIVE").build();
        RecurringPayment net = RecurringPayment.builder().amount(new BigDecimal("1199.00")).frequency("MONTHLY").status("ACTIVE").build();
        when(recurringPaymentRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Arrays.asList(emi, net));

        when(categoryRepository.findByUserIdOrUserIdIsNull(userId)).thenReturn(Collections.emptyList());
        when(transactionRepository.sumExpensesByCategory(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        FinancialSummaryDto summary = analyticsService.getFinancialSummary(userId);

        assertNotNull(summary);
        // Liquid balance = 1,65,000 + 4,00,000 = 5,65,000
        assertEquals(new BigDecimal("565000.00"), summary.getTotalLiquidBalance());
        // Total debt = 24,650
        assertEquals(new BigDecimal("24650.00"), summary.getTotalDebt());
        // Net worth = 5,65,000 - 24,650 = 5,40,350
        assertEquals(new BigDecimal("540350.00"), summary.getNetWorth());
        // Net savings = 1,85,000 - 49,649 = 1,35,351
        assertEquals(new BigDecimal("135351.00"), summary.getNetSavings());
        // Monthly recurring = 33,199
        assertEquals(new BigDecimal("33199.00"), summary.getMonthlyRecurringTotal());

        // Savings rate = (135351 * 100) / 185000 = 73.16%
        assertEquals(new BigDecimal("73.16"), summary.getSavingsRatePercentage());
    }

    @Test
    @DisplayName("Financial Summary should safely handle zero income without divide by zero arithmetic exception")
    void testGetFinancialSummary_ZeroIncomeSafety() {
        when(accountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(eq(userId), eq("CREDIT"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateRange(eq(userId), eq("DEBIT"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("5000.00"));
        when(recurringPaymentRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(Collections.emptyList());
        when(categoryRepository.findByUserIdOrUserIdIsNull(userId)).thenReturn(Collections.emptyList());
        when(transactionRepository.sumExpensesByCategory(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        FinancialSummaryDto summary = analyticsService.getFinancialSummary(userId);

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO, summary.getSavingsRatePercentage());
        assertEquals(new BigDecimal("-5000.00"), summary.getNetSavings());
    }
}
