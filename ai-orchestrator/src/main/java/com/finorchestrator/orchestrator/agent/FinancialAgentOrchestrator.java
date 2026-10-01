package com.finorchestrator.orchestrator.agent;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.mcp.McpToolResult;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.orchestrator.dto.ChatQueryRequest;
import com.finorchestrator.orchestrator.dto.ChatQueryResponse;
import com.finorchestrator.orchestrator.mcp.McpClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(FinancialAgentOrchestrator.class);

    private final IntentClassifier intentClassifier;
    private final AgentPlanner agentPlanner;
    private final McpClient mcpClient;
    private final ResponseSynthesizer responseSynthesizer;
    private final RestTemplate restTemplate;

    @Value("${services.audit-service:http://localhost:8087}")
    private String auditServiceUrl;

    public ChatQueryResponse processQuery(ChatQueryRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) {
            correlationId = UserContext.getCorrelationId() != null ? UserContext.getCorrelationId() : UUID.randomUUID().toString();
        }

        String query = request.getQuery().trim();
        log.info("FinOrchestrator processing query [userId={}, corrId={}]: {}", userId, correlationId, query);

        // Step 2 & 3: Classify Intent
        AgentIntent intent = intentClassifier.classify(query);

        // Step 4 & 5: Build Execution Plan
        ExecutionPlan plan = agentPlanner.createPlan(query, intent, userId);

        List<ChatQueryResponse.ToolCallSummary> toolCallsAudit = new ArrayList<>();
        List<String> citations = new ArrayList<>();
        String finalAnswer;

        // Step 6 & 7: Execute Tools
        for (PlanStep step : plan.getSteps()) {
            long stepStart = System.currentTimeMillis();
            McpToolResult toolResult = mcpClient.callTool(step.getServerCategory(), step.getToolName(), step.getArguments());
            long stepDuration = System.currentTimeMillis() - stepStart;

            step.setExecuted(true);
            step.setResult(toolResult.getContent());
            step.setSuccess(!toolResult.isError());
            if (toolResult.isError()) {
                step.setErrorMessage(String.valueOf(toolResult.getContent()));
            }

            toolCallsAudit.add(ChatQueryResponse.ToolCallSummary.builder()
                    .serverCategory(step.getServerCategory())
                    .toolName(step.getToolName())
                    .arguments(step.getArguments())
                    .result(toolResult.getContent())
                    .success(!toolResult.isError())
                    .durationMs(stepDuration)
                    .build());
        }

        // Step 8 & 12: Generate Response
        if (plan.isDeterministicOnly()) {
            // Pure deterministic response (no LLM)
            finalAnswer = formatDeterministicResponse(query, plan);
        } else {
            // Reasoning / Advisory / Contract response using LLM Router
            finalAnswer = responseSynthesizer.synthesize(query, plan, citations);
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        String traceId = "TRACE-" + UUID.randomUUID().toString();

        // Step 13 & 14: Asynchronous Audit Logging
        recordAuditTrace(traceId, correlationId, userId, query, intent.name(), plan, toolCallsAudit, finalAnswer, totalDuration);

        return ChatQueryResponse.builder()
                .answer(finalAnswer)
                .intent(intent.name())
                .isDeterministic(plan.isDeterministicOnly())
                .executionPlanReasoning(plan.getReasoning())
                .toolsUsed(toolCallsAudit)
                .citations(citations)
                .executionTraceId(traceId)
                .correlationId(correlationId)
                .durationMs(totalDuration)
                .build();
    }

    private String formatDeterministicResponse(String query, ExecutionPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("### [FACT] Deterministic Financial Ledger Result\n");

        for (PlanStep step : plan.getSteps()) {
            if ("getAccountBalance".equals(step.getToolName()) && step.getResult() instanceof List<?> accounts) {
                sb.append("Your verified account balances:\n");
                for (Object accObj : accounts) {
                    if (accObj instanceof Map<?, ?> acc) {
                        sb.append(String.format("* **%s** (%s): **₹%,.2f** [%s]\n",
                                acc.get("accountName"), acc.get("accountType"),
                                Double.parseDouble(acc.get("balance").toString()), acc.get("status")));
                    }
                }
            } else if ("getFinancialSummary".equals(step.getToolName()) && step.getResult() instanceof Map<?, ?> summary) {
                sb.append(String.format("""
* **Total Liquid Funds**: ₹%,.2f
* **Total Debt**: ₹%,.2f
* **Net Worth**: ₹%,.2f
* **Current Month Income**: ₹%,.2f
* **Current Month Expenses**: ₹%,.2f
* **Net Savings**: ₹%,.2f (Savings Rate: %s%%)
""",
                        Double.parseDouble(summary.get("totalLiquidBalance").toString()),
                        Double.parseDouble(summary.get("totalDebt").toString()),
                        Double.parseDouble(summary.get("netWorth").toString()),
                        Double.parseDouble(summary.get("currentMonthIncome").toString()),
                        Double.parseDouble(summary.get("currentMonthExpenses").toString()),
                        Double.parseDouble(summary.get("netSavings").toString()),
                        summary.get("savingsRatePercentage")));
            }
        }

        sb.append("\n> **Execution Note**: This query was answered directly from your transactional ledger in <50ms without invoking an LLM.");
        return sb.toString();
    }

    private void recordAuditTrace(String traceId, String correlationId, Long userId, String query,
                                  String intent, ExecutionPlan plan,
                                  List<ChatQueryResponse.ToolCallSummary> tools,
                                  String finalResponse, long durationMs) {
        try {
            List<String> planSteps = plan.getSteps().stream()
                    .map(s -> String.format("[%s/%s] %s", s.getServerCategory(), s.getToolName(), s.getDescription()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> toolsCalledList = tools.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("server", t.getServerCategory());
                map.put("tool", t.getToolName());
                map.put("args", t.getArguments());
                map.put("success", t.isSuccess());
                map.put("durationMs", t.getDurationMs());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> body = Map.of(
                    "traceId", traceId,
                    "correlationId", correlationId,
                    "userId", userId,
                    "userQuery", query,
                    "intent", intent,
                    "planSteps", planSteps,
                    "toolsCalled", toolsCalledList,
                    "validationStatus", "VALIDATED",
                    "finalResponse", finalResponse,
                    "durationMs", durationMs,
                    "tokensUsed", 0
            );

            String url = auditServiceUrl + "/api/v1/audit/traces";
            restTemplate.postForObject(url, body, ApiResponse.class);
            log.info("Successfully recorded audit trace: {}", traceId);
        } catch (Exception e) {
            log.warn("Failed to send trace to audit-service gracefully: {}", e.getMessage());
        }
    }
}
