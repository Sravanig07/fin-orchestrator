package com.finorchestrator.risk.service;

import com.finorchestrator.risk.dto.CashBufferDto;
import com.finorchestrator.risk.dto.SpendingPatternDto;
import com.finorchestrator.risk.dto.TransactionRiskAnalysisDto;
import com.finorchestrator.risk.repository.RiskEventRepository;
import com.finorchestrator.risk.service.impl.RiskEngineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RiskEngineServiceTest {

    @Mock
    private RiskEventRepository riskEventRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private RiskEngineServiceImpl riskEngineService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(riskEngineService, "financeServiceUrl", "http://localhost:8082");
    }

    @Test
    @DisplayName("Cash Buffer should deterministically calculate months of runway and reserve surplus")
    void testCalculateCashBuffer_DeterministicMetrics() {
        // Fallback baseline: 5,65,000 liquid, 85,000 monthly burn
        CashBufferDto buffer = riskEngineService.calculateCashBuffer(1L);

        assertNotNull(buffer);
        // 565000 / 85000 = 6.65
        assertEquals(new BigDecimal("6.65"), buffer.getBufferMonths());
        assertEquals("STRONG", buffer.getRunwayStatus());
        // 3 months recommended buffer = 85,000 * 3 = 255,000
        assertEquals(new BigDecimal("255000.00"), buffer.getMinimumRecommendedBuffer());
        // Surplus = 565,000 - 255,000 = 310,000
        assertEquals(new BigDecimal("310000.00"), buffer.getBufferDeficitOrSurplus());
    }

    @Test
    @DisplayName("Analyze Transaction should flag HIGH risk when large transaction depletes buffer below safe threshold")
    void testAnalyzeTransaction_HighRiskDepletion() {
        // Current liquid is 5,65,000, monthly burn is 85,000
        // A huge expense of 4,50,000 leaves only 1,15,000 (< 2 months burn of 1,70,000)
        BigDecimal largeExpense = new BigDecimal("450000.00");

        TransactionRiskAnalysisDto analysis = riskEngineService.analyzeTransaction(1L, 999L, largeExpense);

        assertNotNull(analysis);
        assertTrue(analysis.isRiskFlagged());
        assertEquals("HIGH", analysis.getRiskSeverity());
        // Post tx buffer: 115000 / 85000 = 1.35 months
        assertEquals(new BigDecimal("1.35"), analysis.getPostTransactionEstimatedBufferMonths());
        assertTrue(analysis.getTriggerReasons().stream().anyMatch(r -> r.contains("50% of monthly burn rate")));
        assertTrue(analysis.getTriggerReasons().stream().anyMatch(r -> r.contains("< 2.0 months safe threshold")));
    }

    @Test
    @DisplayName("Analyze Transaction should NOT flag risk for normal everyday expense")
    void testAnalyzeTransaction_SafeExpense() {
        // Small expense: ₹1,500
        BigDecimal normalExpense = new BigDecimal("1500.00");

        TransactionRiskAnalysisDto analysis = riskEngineService.analyzeTransaction(1L, 1000L, normalExpense);

        assertNotNull(analysis);
        assertFalse(analysis.isRiskFlagged());
        assertEquals("NONE", analysis.getRiskSeverity());
        assertTrue(analysis.getPostTransactionEstimatedBufferMonths().compareTo(new BigDecimal("6.0")) > 0);
    }

    @Test
    @DisplayName("Analyze Spending Pattern should identify deviations from historical baseline")
    void testAnalyzeSpendingPattern() {
        SpendingPatternDto pattern = riskEngineService.analyzeSpendingPattern(1L);

        assertNotNull(pattern);
        assertEquals(new BigDecimal("49649.00"), pattern.getCurrentMonthSpending());
        assertEquals(new BigDecimal("42000.00"), pattern.getAveragePreviousMonthlySpending());
        // (49649 - 42000) / 42000 * 100 = 18.21%
        assertEquals(new BigDecimal("18.21"), pattern.getPercentageChange());
        assertFalse(pattern.isAnomalyDetected()); // 18.21% <= 20.00% threshold
    }
}
