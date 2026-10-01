package com.finorchestrator.mcp.risk;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.mcp.McpToolDefinition;
import com.finorchestrator.common.mcp.McpToolResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RiskMcpTools {

    private static final Logger log = LoggerFactory.getLogger(RiskMcpTools.class);

    private final RestTemplate restTemplate;

    @Value("${services.risk-service:http://localhost:8085}")
    private String riskServiceUrl;

    public List<McpToolDefinition> getToolDefinitions() {
        return List.of(
                McpToolDefinition.builder()
                        .name("analyzeTransaction")
                        .description("Evaluates if a specific proposed or executed transaction triggers anomaly or risk thresholds")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "amount", Map.of("type", "number", "description", "Transaction amount"),
                                        "transactionId", Map.of("type", "integer", "description", "Optional transaction ID")
                                ),
                                "required", List.of("userId", "amount")
                        )).build(),

                McpToolDefinition.builder()
                        .name("detectRiskSignals")
                        .description("Returns all active liquidity, overdraft, and contract risk signals for the user")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("calculateCashBuffer")
                        .description("Calculates runway in months = liquid cash / average monthly expenses")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("analyzeSpendingPattern")
                        .description("Performs spending anomaly detection across expense categories against rolling baselines")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("analyzeFinancialObligations")
                        .description("Calculates debt-to-income and recurring payment burden against monthly income")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing Risk MCP Tool [{}] with args: {}", toolName, args);
        Long userId = args.get("userId") != null ? Long.parseLong(args.get("userId").toString()) : 1L;

        try {
            return switch (toolName) {
                case "calculateCashBuffer" -> {
                    String url = riskServiceUrl + "/api/v1/risk/cash-buffer";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "detectRiskSignals" -> {
                    String url = riskServiceUrl + "/api/v1/risk/signals";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "analyzeSpendingPattern" -> {
                    String url = riskServiceUrl + "/api/v1/risk/spending-pattern";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "analyzeFinancialObligations" -> {
                    String url = riskServiceUrl + "/api/v1/risk/obligations";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "analyzeTransaction" -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("userId", userId);
                    body.put("amount", args.get("amount"));
                    if (args.containsKey("transactionId")) {
                        body.put("transactionId", args.get("transactionId"));
                    }
                    String url = riskServiceUrl + "/api/v1/risk/analyze-transaction";
                    var resp = restTemplate.postForObject(url, body, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                default -> McpToolResult.error("Unknown risk tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Failed executing risk tool [{}]: {}", toolName, e.getMessage());
            return McpToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
}
