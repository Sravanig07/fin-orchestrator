package com.finorchestrator.finance.service.impl;

import com.finorchestrator.finance.dto.CashFlowAnalysisDto;
import com.finorchestrator.finance.dto.FinancialSummaryDto;
import com.finorchestrator.finance.dto.RecurringPaymentDto;
import com.finorchestrator.finance.entity.Account;
import com.finorchestrator.finance.entity.Category;
import com.finorchestrator.finance.entity.RecurringPayment;
import com.finorchestrator.finance.entity.Transaction;
import com.finorchestrator.finance.repository.AccountRepository;
import com.finorchestrator.finance.repository.CategoryRepository;
import com.finorchestrator.finance.repository.RecurringPaymentRepository;
import com.finorchestrator.finance.repository.TransactionRepository;
import com.finorchestrator.finance.service.FinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAnalyticsServiceImpl implements FinancialAnalyticsService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryDto getFinancialSummary(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        BigDecimal liquidBalance = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;

        for (Account a : accounts) {
            BigDecimal bal = a.getBalance();
            if ("CREDIT_CARD".equalsIgnoreCase(a.getAccountType())) {
                if (bal.compareTo(BigDecimal.ZERO) < 0) {
                    totalDebt = totalDebt.add(bal.abs());
                }
            } else if ("CHECKING".equalsIgnoreCase(a.getAccountType()) || "SAVINGS".equalsIgnoreCase(a.getAccountType())) {
                if (bal.compareTo(BigDecimal.ZERO) > 0) {
                    liquidBalance = liquidBalance.add(bal);
                }
            }
        }

        BigDecimal netWorth = liquidBalance.subtract(totalDebt);

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        BigDecimal monthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, "CREDIT", startOfMonth, endOfMonth);
        BigDecimal monthExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, "DEBIT", startOfMonth, endOfMonth);
        BigDecimal netSavings = monthIncome.subtract(monthExpenses);

        BigDecimal savingsRate = BigDecimal.ZERO;
        if (monthIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.multiply(BigDecimal.valueOf(100))
                    .divide(monthIncome, 2, RoundingMode.HALF_UP);
        }

        // Active monthly recurring obligations
        List<RecurringPayment> activeRecurring = recurringPaymentRepository.findByUserIdAndStatus(userId, "ACTIVE");
        BigDecimal monthlyRecurring = activeRecurring.stream()
                .map(r -> normalizeToMonthly(r.getAmount(), r.getFrequency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Expense by Category Map
        List<Category> allCategories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        Map<Long, String> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        List<Object[]> categorySums = transactionRepository.sumExpensesByCategory(userId, startOfMonth, endOfMonth);
        Map<String, BigDecimal> expensesByCategory = new LinkedHashMap<>();
        for (Object[] row : categorySums) {
            Long catId = ((Number) row[0]).longValue();
            BigDecimal sum = (BigDecimal) row[1];
            String catName = categoryMap.getOrDefault(catId, "Category #" + catId);
            expensesByCategory.put(catName, sum);
        }

        return FinancialSummaryDto.builder()
                .userId(userId)
                .totalLiquidBalance(liquidBalance)
                .totalDebt(totalDebt)
                .netWorth(netWorth)
                .currentMonthIncome(monthIncome)
                .currentMonthExpenses(monthExpenses)
                .netSavings(netSavings)
                .monthlyRecurringTotal(monthlyRecurring)
                .savingsRatePercentage(savingsRate)
                .expensesByCategory(expensesByCategory)
                .calculatedAt(Instant.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CashFlowAnalysisDto getCashFlowAnalysis(Long userId, LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);

        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;

        Map<LocalDate, BigDecimal[]> dailyMap = new TreeMap<>();
        for (Transaction t : transactions) {
            dailyMap.putIfAbsent(t.getTransactionDate(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] entry = dailyMap.get(t.getTransactionDate());
            if ("CREDIT".equalsIgnoreCase(t.getType())) {
                totalInflow = totalInflow.add(t.getAmount());
                entry[0] = entry[0].add(t.getAmount());
            } else {
                totalOutflow = totalOutflow.add(t.getAmount());
                entry[1] = entry[1].add(t.getAmount());
            }
        }

        BigDecimal netCashFlow = totalInflow.subtract(totalOutflow);

        long daysElapsed = ChronoUnit.DAYS.between(start, end) + 1;
        if (daysElapsed < 1) daysElapsed = 1;

        BigDecimal avgDailyBurn = totalOutflow.divide(BigDecimal.valueOf(daysElapsed), 2, RoundingMode.HALF_UP);

        YearMonth yearMonth = YearMonth.from(end);
        int totalDaysInMonth = yearMonth.lengthOfMonth();
        int currentDay = end.getDayOfMonth();
        int daysRemaining = Math.max(0, totalDaysInMonth - currentDay);

        FinancialSummaryDto summary = getFinancialSummary(userId);
        BigDecimal projectedMonthEnd = summary.getTotalLiquidBalance().subtract(
                avgDailyBurn.multiply(BigDecimal.valueOf(daysRemaining))
        );

        List<CashFlowAnalysisDto.DailyCashFlowPoint> points = dailyMap.entrySet().stream()
                .map(e -> CashFlowAnalysisDto.DailyCashFlowPoint.builder()
                        .date(e.getKey())
                        .inflow(e.getValue()[0])
                        .outflow(e.getValue()[1])
                        .net(e.getValue()[0].subtract(e.getValue()[1]))
                        .build())
                .collect(Collectors.toList());

        return CashFlowAnalysisDto.builder()
                .userId(userId)
                .periodStart(start)
                .periodEnd(end)
                .totalInflow(totalInflow)
                .totalOutflow(totalOutflow)
                .netCashFlow(netCashFlow)
                .averageDailyBurnRate(avgDailyBurn)
                .projectedMonthEndBalance(projectedMonthEnd)
                .daysElapsed((int) daysElapsed)
                .daysRemaining(daysRemaining)
                .trend(points)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyExpenses(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, "DEBIT", ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyIncome(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, "CREDIT", ym.atDay(1), ym.atEndOfMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringPaymentDto> getRecurringPayments(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        Map<Long, String> accountMap = accounts.stream().collect(Collectors.toMap(Account::getId, Account::getAccountName, (a, b) -> a));

        List<Category> categories = categoryRepository.findByUserIdOrUserIdIsNull(userId);
        Map<Long, String> catMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        return recurringPaymentRepository.findByUserId(userId).stream()
                .map(r -> RecurringPaymentDto.builder()
                        .id(r.getId())
                        .userId(r.getUserId())
                        .accountId(r.getAccountId())
                        .accountName(accountMap.getOrDefault(r.getAccountId(), "Account #" + r.getAccountId()))
                        .categoryId(r.getCategoryId())
                        .categoryName(catMap.getOrDefault(r.getCategoryId(), "General"))
                        .merchant(r.getMerchant())
                        .amount(r.getAmount())
                        .frequency(r.getFrequency())
                        .nextDueDate(r.getNextDueDate())
                        .status(r.getStatus())
                        .autoDebit(r.getAutoDebit())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal normalizeToMonthly(BigDecimal amount, String frequency) {
        if (frequency == null) return amount;
        return switch (frequency.toUpperCase()) {
            case "WEEKLY" -> amount.multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case "QUARTERLY" -> amount.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case "YEARLY" -> amount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            default -> amount; // MONTHLY
        };
    }
}
