package com.finorchestrator.orchestrator.llm.impl;

import com.finorchestrator.orchestrator.llm.LLMProvider;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("mockLLMProvider")
public class MockLLMProvider implements LLMProvider {

    @Override
    public String generateCompletion(String systemPrompt, String userPrompt) {
        String lower = userPrompt.toLowerCase();

        // 1. Affordability Scenario: "Can I afford to spend ₹60,000 this month?"
        if (lower.contains("afford") || lower.contains("60,000") || lower.contains("spend")) {
            return """
### [CALCULATION] Affordability Analysis
Based on your validated financial data for this month:
* **[FACT] Current Liquid Balance**: ₹5,65,000 across your HDFC Salary Checking and ICICI Savings accounts.
* **[FACT] Monthly Net Income**: ₹1,85,000.
* **[FACT] Month-to-Date Expenses**: ₹49,649.
* **[FACT] Upcoming Recurring Obligations**: ₹36,348 (Home Loan EMI ₹32,000, Airtel ₹1,199, Cult.fit ₹2,500, Netflix ₹649).
* **[CALCULATION] Projected Remaining Discretionary Surplus**: ₹98,903.
* **[CALCULATION] Cash Buffer Runway**: 4.78 months of expenses.

### [AI_GENERATED_INTERPRETATION] Recommendation
**Yes, you can afford to spend ₹60,000 this month.**
Even after this discretionary expenditure of ₹60,000 and settling all scheduled monthly obligations (₹36,348), your liquid buffer will remain at **₹5,05,000**, which still provides **4.28 months of emergency runway** (well above your recommended 3.0-month threshold).

> **Source Data**: Financial MCP Server (`getAccountBalance`, `getCashFlow`, `getRecurringPayments`), Risk MCP Server (`calculateCashBuffer`).
""";
        }

        // 2. Spending increase: "Why did my spending increase?"
        if (lower.contains("why") && (lower.contains("increase") || lower.contains("spending") || lower.contains("expenses"))) {
            return """
### [CALCULATION] Spending Variance Analysis
Comparing your current month's expenses against your 3-month baseline:
* **[FACT] Current Month Total**: ₹49,649 (vs. historical average of ₹42,000, a **+18.2% increase**).
* **[FACT] Category Breakdown**:
  * **Dining & Food Delivery**: ₹5,250 (+50.0% variance over baseline of ₹3,500) — *Triggered Spending Anomaly*.
  * **Housing & Rent**: ₹32,000 (Consistent, Home Loan EMI).
  * **Groceries**: ₹7,350 (Normal variance).
  * **Transport & Fuel**: ₹2,500 (Normal variance).

### [AI_GENERATED_INTERPRETATION] Explanation
The primary driver of your spending increase is **Dining & Food Delivery**, with multiple high-value weekend restaurant orders (Smoke House Deli ₹1,850, Toit Brewery ₹3,400) pushing this category 50% above your historical average.

> **Source Data**: Finance MCP Server (`getMonthlyExpenses`), Risk MCP Server (`analyzeSpendingPattern`).
""";
        }

        // 3. Contract penalty query: "What penalties are in my loan contract?"
        if (lower.contains("penalty") || lower.contains("penalties") || lower.contains("prepayment")) {
            return """
### [CONTRACT_INFO] Extracted Contract Clauses
Based on your active **HDFC Home Loan Facility Agreement** (Document ID: `DOC-HDFC-HL-2024`):

1. **Prepayment Penalty**:
   * **[CONTRACT_INFO] Clause**: *Section 8.2 (Page 14, Chunk `CHUNK-DOC-HDFC-HL-2024-P14-01`)*
   * **Rule**: **0.00% Prepayment Penalty**. In compliance with Reserve Bank of India (RBI) guidelines, no prepayment charges or penalty shall be levied on early repayment of floating rate term loans sanctioned to individual borrowers.

2. **Default / Late Payment Penalty**:
   * **[CONTRACT_INFO] Clause**: *Section 11.4 (Page 19, Chunk `CHUNK-DOC-HDFC-HL-2024-P19-01`)*
   * **Rule**: **2% per month (24% per annum)** penal interest on overdue installments payable monthly until actual settlement.

### [AI_GENERATED_INTERPRETATION] Takeaway
You can make prepayments of any amount towards your principal at any time without any financial penalty. However, overdue EMIs incur penal interest at 24% p.a.

> **Source Reference**: Contract MCP Server (`getPenalties`, `searchContractClauses`).
""";
        }

        // 4. Contract expiry: "When does my contract expire?"
        if (lower.contains("expire") || lower.contains("expiry") || lower.contains("renewal")) {
            return """
### [CONTRACT_INFO] Upcoming Contract Expirations & Renewals
* **[FACT] Star Health Comprehensive Insurance Policy**:
  * **Contract ID**: `2`
  * **Renewal Due Date**: **2026-10-15** (24 days remaining)
  * **Annual Renewal Premium**: ₹24,800
  * **Grace Period**: 30 days from expiry (*Section 4.1, Page 8*)
* **[FACT] American Express Platinum Reserve**:
  * **Annual Fee Due Date**: 2027-06-01 (Annual Fee ₹60,000)

> **Source Reference**: Contract MCP Server (`getContractExpiry`).
""";
        }

        // Default structured synthesis
        return String.format("""
### [AI_GENERATED_INTERPRETATION] Financial Synthesis
Based on verified records from your financial accounts and active agreements:
* All figures have been validated deterministically through the MCP Tool Layer.
* Query processed: "%s"

> **Status**: Verified by FinOrchestrator Deterministic Engine.
""", userPrompt);
    }

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
