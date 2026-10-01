package com.finorchestrator.mcp.finance;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.mcp.McpToolDefinition;
import com.finorchestrator.common.mcp.McpToolResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FinanceMcpTools {

    private static final Logger log = LoggerFactory.getLogger(FinanceMcpTools.class);

    private final RestTemplate restTemplate;

    @Value("${services.finance-service:http://localhost:8082}")
    private String financeServiceUrl;

    public List<McpToolDefinition> getToolDefinitions() {
        return List.of(
                McpToolDefinition.builder()
                        .name("getAccountBalance")
                        .description("Fetches all accounts and their current balances for the user")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getTransactions")
                        .description("Retrieves paginated transactions for the user")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "page", Map.of("type", "integer", "description", "Page number, 0-indexed"),
                                        "size", Map.of("type", "integer", "description", "Page size")
                                ),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("searchTransactions")
                        .description("Searches transactions by description, merchant, or notes")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "query", Map.of("type", "string", "description", "Search query")
                                ),
                                "required", List.of("userId", "query")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getMonthlyExpenses")
                        .description("Returns deterministic total expenses for a specific month and year")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "year", Map.of("type", "integer", "description", "Year (e.g. 2026)"),
                                        "month", Map.of("type", "integer", "description", "Month (1-12)")
                                ),
                                "required", List.of("userId", "year", "month")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getMonthlyIncome")
                        .description("Returns deterministic total income for a specific month and year")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "year", Map.of("type", "integer", "description", "Year (e.g. 2026)"),
                                        "month", Map.of("type", "integer", "description", "Month (1-12)")
                                ),
                                "required", List.of("userId", "year", "month")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getCashFlow")
                        .description("Calculates inflow, outflow, net cash flow, and daily burn rate")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "from", Map.of("type", "string", "description", "Start date (YYYY-MM-DD)"),
                                        "to", Map.of("type", "string", "description", "End date (YYYY-MM-DD)")
                                ),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getRecurringPayments")
                        .description("Lists all active recurring subscriptions and scheduled bills")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getFinancialSummary")
                        .description("Returns comprehensive deterministic summary: liquid balance, debt, net worth, savings rate, and category expenses")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing Financial MCP Tool [{}] with args: {}", toolName, args);
        Long userId = args.get("userId") != null ? Long.parseLong(args.get("userId").toString()) : 1L;

        try {
            return switch (toolName) {
                case "getAccountBalance" -> {
                    String url = financeServiceUrl + "/api/v1/finance/accounts";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getTransactions" -> {
                    int page = args.get("page") != null ? Integer.parseInt(args.get("page").toString()) : 0;
                    int size = args.get("size") != null ? Integer.parseInt(args.get("size").toString()) : 20;
                    String url = String.format("%s/api/v1/finance/transactions?page=%d&size=%d", financeServiceUrl, page, size);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "searchTransactions" -> {
                    String q = (String) args.get("query");
                    String url = String.format("%s/api/v1/finance/transactions/search?q=%s", financeServiceUrl, q);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getMonthlyExpenses" -> {
                    int year = Integer.parseInt(args.get("year").toString());
                    int month = Integer.parseInt(args.get("month").toString());
                    String url = String.format("%s/api/v1/finance/analytics/monthly-expenses?year=%d&month=%d", financeServiceUrl, year, month);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Map.of("totalExpenses", 0));
                }
                case "getMonthlyIncome" -> {
                    int year = Integer.parseInt(args.get("year").toString());
                    int month = Integer.parseInt(args.get("month").toString());
                    String url = String.format("%s/api/v1/finance/analytics/monthly-income?year=%d&month=%d", financeServiceUrl, year, month);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Map.of("totalIncome", 0));
                }
                case "getCashFlow" -> {
                    String from = (String) args.get("from");
                    String to = (String) args.get("to");
                    String url = financeServiceUrl + "/api/v1/finance/analytics/cash-flow";
                    if (from != null && to != null) {
                        url += String.format("?from=%s&to=%s", from, to);
                    }
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "getRecurringPayments" -> {
                    String url = financeServiceUrl + "/api/v1/finance/analytics/recurring";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getFinancialSummary" -> {
                    String url = financeServiceUrl + "/api/v1/finance/analytics/summary";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                default -> McpToolResult.error("Unknown tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Failed executing tool [{}]: {}", toolName, e.getMessage());
            return McpToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
}
