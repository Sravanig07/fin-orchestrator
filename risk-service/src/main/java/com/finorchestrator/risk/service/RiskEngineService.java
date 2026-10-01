package com.finorchestrator.risk.service;

import com.finorchestrator.risk.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface RiskEngineService {
    CashBufferDto calculateCashBuffer(Long userId);
    SpendingPatternDto analyzeSpendingPattern(Long userId);
    FinancialObligationsDto analyzeFinancialObligations(Long userId);
    List<RiskSignalDto> detectRiskSignals(Long userId);
    TransactionRiskAnalysisDto analyzeTransaction(Long userId, Long transactionId, BigDecimal amount);
}
