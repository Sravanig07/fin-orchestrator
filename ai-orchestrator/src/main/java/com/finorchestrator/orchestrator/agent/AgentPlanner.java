package com.finorchestrator.orchestrator.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgentPlanner {

    private static final Logger log = LoggerFactory.getLogger(AgentPlanner.class);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)(₹|inr|rs\\.?|\\$)?\\s*([0-9]{1,3}(,[0-9]{3})*(\\.[0-9]+)?|[0-9]+)");

    public ExecutionPlan createPlan(String query, AgentIntent intent, Long userId) {
        log.info("Creating execution plan for intent [{}] and query: {}", intent, query);

        ExecutionPlan plan = ExecutionPlan.builder()
                .query(query)
                .intent(intent)
                .build();

        int stepNum = 1;

        switch (intent) {
            case DETERMINISTIC_FINANCIAL -> {
                plan.setDeterministicOnly(true);
                plan.setReasoning("Pure financial lookup. Bypassing LLM generation to prevent hallucinations.");

                if (query.toLowerCase().contains("spend") || query.toLowerCase().contains("expense")) {
                    plan.addStep(PlanStep.builder()
                            .stepNumber(stepNum++)
                            .serverCategory("finance")
                            .toolName("getFinancialSummary")
                            .description("Retrieve deterministic monthly expenses and category breakdown")
                            .arguments(Map.of("userId", userId))
                            .build());
                } else {
                    plan.addStep(PlanStep.builder()
                            .stepNumber(stepNum++)
                            .serverCategory("finance")
                            .toolName("getAccountBalance")
                            .description("Retrieve exact account balances from ledger")
                            .arguments(Map.of("userId", userId))
                            .build());
                }
            }

            case FINANCIAL_ADVISORY -> {
                plan.setDeterministicOnly(false);
                plan.setReasoning("Multi-step affordability and cash runway evaluation requiring live ledger figures and risk rules.");

                BigDecimal extractedAmount = extractAmount(query);

                // Step 1: Get liquid account balance
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("finance")
                        .toolName("getAccountBalance")
                        .description("Fetch current liquid funds across all checking and savings accounts")
                        .arguments(Map.of("userId", userId))
                        .build());

                // Step 2: Get cash flow trajectory
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("finance")
                        .toolName("getCashFlow")
                        .description("Calculate current month inflows, outflows, and average daily burn rate")
                        .arguments(Map.of("userId", userId))
                        .build());

                // Step 3: Get fixed recurring payment obligations
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("finance")
                        .toolName("getRecurringPayments")
                        .description("Fetch upcoming scheduled EMI, rent, and subscription obligations")
                        .arguments(Map.of("userId", userId))
                        .build());

                // Step 4: Calculate liquid runway
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("risk")
                        .toolName("calculateCashBuffer")
                        .description("Calculate liquid emergency runway in months")
                        .arguments(Map.of("userId", userId))
                        .build());

                // Step 5: If a specific amount was asked (e.g. ₹60,000), simulate transaction impact
                if (extractedAmount != null) {
                    plan.addStep(PlanStep.builder()
                            .stepNumber(stepNum++)
                            .serverCategory("risk")
                            .toolName("analyzeTransaction")
                            .description("Test impact of proposed transaction on liquidity buffer threshold")
                            .arguments(Map.of("userId", userId, "amount", extractedAmount))
                            .build());
                }
            }

            case CONTRACT_INTELLIGENCE -> {
                plan.setDeterministicOnly(false);
                plan.setReasoning("Document and legal contract analysis requiring structured terms and vector RAG clause search.");

                // Step 1: Search contract metadata
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("contract")
                        .toolName("searchContracts")
                        .description("Identify active user contracts matching the query")
                        .arguments(Map.of("userId", userId, "query", query))
                        .build());

                // Step 2: If query asks about penalties, fetch structured penalties
                if (query.toLowerCase().contains("penalty") || query.toLowerCase().contains("penalties") || query.toLowerCase().contains("late")) {
                    plan.addStep(PlanStep.builder()
                            .stepNumber(stepNum++)
                            .serverCategory("contract")
                            .toolName("getPenalties")
                            .description("Retrieve extracted contractual penalties and fees")
                            .arguments(Map.of("userId", userId, "contractId", 1L))
                            .build());
                }

                // Step 3: Vector RAG search for relevant clause text with page citation
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("contract")
                        .toolName("searchContractClauses")
                        .description("Perform semantic vector search on document chunks to locate exact clauses and page numbers")
                        .arguments(Map.of("userId", userId, "query", query))
                        .build());
            }

            case RISK_ASSESSMENT -> {
                plan.setDeterministicOnly(false);
                plan.setReasoning("Evaluating spending variance, category anomalies, and liquidity risk.");

                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("risk")
                        .toolName("detectRiskSignals")
                        .description("Query all active risk signals and warning alerts")
                        .arguments(Map.of("userId", userId))
                        .build());

                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("risk")
                        .toolName("calculateCashBuffer")
                        .description("Calculate current cash buffer in months of burn rate")
                        .arguments(Map.of("userId", userId))
                        .build());

                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("risk")
                        .toolName("analyzeSpendingPattern")
                        .description("Detect variance spikes across expense categories compared to baseline")
                        .arguments(Map.of("userId", userId))
                        .build());
            }

            case NOTIFICATION_MANAGEMENT -> {
                plan.setDeterministicOnly(false);
                plan.setReasoning("Reminder and notification management.");

                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("notification")
                        .toolName("listReminders")
                        .description("Retrieve active reminders and alerts")
                        .arguments(Map.of("userId", userId))
                        .build());
            }

            default -> {
                plan.setDeterministicOnly(true);
                plan.addStep(PlanStep.builder()
                        .stepNumber(stepNum++)
                        .serverCategory("finance")
                        .toolName("getFinancialSummary")
                        .description("Fetch high-level financial summary")
                        .arguments(Map.of("userId", userId))
                        .build());
            }
        }

        return plan;
    }

    private BigDecimal extractAmount(String query) {
        Matcher matcher = AMOUNT_PATTERN.matcher(query);
        BigDecimal found = null;
        while (matcher.find()) {
            String valStr = matcher.group(2).replace(",", "");
            try {
                BigDecimal val = new BigDecimal(valStr);
                if (val.compareTo(BigDecimal.valueOf(100)) >= 0) { // filter trivial single digits
                    found = val;
                }
            } catch (Exception ignored) {}
        }
        return found;
    }
}
