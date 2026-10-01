package com.finorchestrator.orchestrator.mcp;

import com.finorchestrator.common.mcp.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class McpClient {

    private static final Logger log = LoggerFactory.getLogger(McpClient.class);

    private final RestTemplate restTemplate;

    @Value("${mcp.servers.finance:http://localhost:8091}")
    private String financeMcpUrl;

    @Value("${mcp.servers.contract:http://localhost:8092}")
    private String contractMcpUrl;

    @Value("${mcp.servers.risk:http://localhost:8093}")
    private String riskMcpUrl;

    @Value("${mcp.servers.notification:http://localhost:8094}")
    private String notificationMcpUrl;

    @CircuitBreaker(name = "mcpTools", fallbackMethod = "mcpFallback")
    @Retry(name = "mcpTools")
    public McpToolResult callTool(String serverCategory, String toolName, Map<String, Object> arguments) {
        String targetBaseUrl = resolveServerUrl(serverCategory);
        String endpoint = targetBaseUrl + "/mcp";

        log.info("MCP Client calling [{} / {}] at {}", serverCategory, toolName, endpoint);

        McpRequest request = McpRequest.builder()
                .jsonrpc("2.0")
                .id(UUID.randomUUID().toString())
                .method("tools/call")
                .params(Map.of(
                        "name", toolName,
                        "arguments", arguments != null ? arguments : Collections.emptyMap()
                ))
                .build();

        try {
            McpResponse response = restTemplate.postForObject(endpoint, request, McpResponse.class);
            if (response == null || response.getError() != null) {
                String errMsg = response != null && response.getError() != null
                        ? response.getError().getMessage()
                        : "Empty MCP response";
                log.warn("MCP Tool [{}] returned error: {}", toolName, errMsg);
                return McpToolResult.error(errMsg);
            }

            // Unpack result
            if (response.getResult() instanceof Map<?, ?> mapResult) {
                return McpToolResult.builder()
                        .content(mapResult.get("content"))
                        .isError(Boolean.TRUE.equals(mapResult.get("isError")))
                        .build();
            }

            return McpToolResult.success(response.getResult());
        } catch (Exception e) {
            log.error("MCP call to [{} / {}] failed: {}", serverCategory, toolName, e.getMessage());
            throw new RuntimeException("MCP Server Error: " + e.getMessage(), e);
        }
    }

    public McpToolResult mcpFallback(String serverCategory, String toolName, Map<String, Object> arguments, Throwable t) {
        log.warn("Resilience4j Fallback triggered for MCP [{} / {}]: {}", serverCategory, toolName, t.getMessage());
        return McpToolResult.error("Service temporarily degraded (fallback): " + t.getMessage());
    }

    private String resolveServerUrl(String serverCategory) {
        return switch (serverCategory.toLowerCase()) {
            case "finance" -> financeMcpUrl;
            case "contract" -> contractMcpUrl;
            case "risk" -> riskMcpUrl;
            case "notification" -> notificationMcpUrl;
            default -> throw new IllegalArgumentException("Unknown MCP server category: " + serverCategory);
        };
    }
}
