package com.finorchestrator.mcp.contract;

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
public class ContractMcpTools {

    private static final Logger log = LoggerFactory.getLogger(ContractMcpTools.class);

    private final RestTemplate restTemplate;

    @Value("${services.contract-service:http://localhost:8083}")
    private String contractServiceUrl;

    @Value("${services.document-service:http://localhost:8084}")
    private String documentServiceUrl;

    public List<McpToolDefinition> getToolDefinitions() {
        return List.of(
                McpToolDefinition.builder()
                        .name("searchContracts")
                        .description("Searches contracts by keyword, counterparty, or title")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "query", Map.of("type", "string", "description", "Search query")
                                ),
                                "required", List.of("userId", "query")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getContract")
                        .description("Retrieves full details of a specific contract by ID")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "contractId", Map.of("type", "integer", "description", "Contract ID")
                                ),
                                "required", List.of("userId", "contractId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getContractTerms")
                        .description("Retrieves extracted financial terms (interest rates, limits, tenures) for a contract")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "contractId", Map.of("type", "integer", "description", "Contract ID")
                                ),
                                "required", List.of("userId", "contractId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getPaymentObligations")
                        .description("Retrieves payment obligations and scheduled installments across contracts")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getContractExpiry")
                        .description("Retrieves upcoming contract expirations and renewal windows")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "daysAhead", Map.of("type", "integer", "description", "Days ahead to look for renewals, default 60")
                                ),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getFees")
                        .description("Retrieves all annual, maintenance, and processing fees in a contract")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "contractId", Map.of("type", "integer", "description", "Contract ID")
                                ),
                                "required", List.of("userId", "contractId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("getPenalties")
                        .description("Retrieves all late payment, prepayment, cancellation, or default penalty clauses")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "contractId", Map.of("type", "integer", "description", "Contract ID")
                                ),
                                "required", List.of("userId", "contractId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("searchContractClauses")
                        .description("Performs semantic vector search across document text chunks, returning relevant legal clauses with exact page numbers and chunk citations")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "query", Map.of("type", "string", "description", "Natural language clause query"),
                                        "contractId", Map.of("type", "integer", "description", "Optional contract filter")
                                ),
                                "required", List.of("userId", "query")
                        )).build()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing Contract MCP Tool [{}] with args: {}", toolName, args);
        Long userId = args.get("userId") != null ? Long.parseLong(args.get("userId").toString()) : 1L;

        try {
            return switch (toolName) {
                case "searchContracts" -> {
                    String q = (String) args.get("query");
                    String url = String.format("%s/api/v1/contracts/search?q=%s", contractServiceUrl, q);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getContract" -> {
                    Long cid = Long.parseLong(args.get("contractId").toString());
                    String url = String.format("%s/api/v1/contracts/%d", contractServiceUrl, cid);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "getContractTerms" -> {
                    Long cid = Long.parseLong(args.get("contractId").toString());
                    String url = String.format("%s/api/v1/contracts/%d/terms", contractServiceUrl, cid);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getPaymentObligations" -> {
                    String url = contractServiceUrl + "/api/v1/contracts";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getContractExpiry" -> {
                    int days = args.get("daysAhead") != null ? Integer.parseInt(args.get("daysAhead").toString()) : 60;
                    String url = String.format("%s/api/v1/contracts/expiring?daysAhead=%d", contractServiceUrl, days);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getFees" -> {
                    Long cid = Long.parseLong(args.get("contractId").toString());
                    String url = String.format("%s/api/v1/contracts/%d/terms", contractServiceUrl, cid);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "getPenalties" -> {
                    Long cid = Long.parseLong(args.get("contractId").toString());
                    String url = String.format("%s/api/v1/contracts/%d/penalties", contractServiceUrl, cid);
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "searchContractClauses" -> {
                    String query = (String) args.get("query");
                    Long cid = args.get("contractId") != null ? Long.parseLong(args.get("contractId").toString()) : null;

                    Map<String, Object> reqBody = new HashMap<>();
                    reqBody.put("query", query);
                    if (cid != null) reqBody.put("contractId", cid);
                    reqBody.put("topK", 3);
                    reqBody.put("minScore", 0.35);

                    String url = documentServiceUrl + "/api/v1/documents/search";
                    var resp = restTemplate.postForObject(url, reqBody, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                default -> McpToolResult.error("Unknown contract tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Failed executing contract tool [{}]: {}", toolName, e.getMessage());
            return McpToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
}
