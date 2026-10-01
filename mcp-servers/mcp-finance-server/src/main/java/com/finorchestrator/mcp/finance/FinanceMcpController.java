package com.finorchestrator.mcp.finance;

import com.finorchestrator.common.mcp.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FinanceMcpController {

    private final FinanceMcpTools financeMcpTools;

    @PostMapping("/mcp")
    public ResponseEntity<McpResponse> handleMcpRequest(@RequestBody McpRequest request) {
        String method = request.getMethod();

        if ("tools/list".equals(method)) {
            List<McpToolDefinition> tools = financeMcpTools.getToolDefinitions();
            return ResponseEntity.ok(McpResponse.success(request.getId(), Map.of("tools", tools)));
        }

        if ("tools/call".equals(method)) {
            Map<String, Object> params = request.getParams();
            if (params == null || !params.containsKey("name")) {
                return ResponseEntity.ok(McpResponse.error(request.getId(), McpError.INVALID_PARAMS, "Missing tool name in params", null));
            }

            String toolName = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Collections.emptyMap());

            McpToolResult result = financeMcpTools.executeTool(toolName, arguments);
            return ResponseEntity.ok(McpResponse.success(request.getId(), result));
        }

        return ResponseEntity.ok(McpResponse.error(request.getId(), McpError.METHOD_NOT_FOUND, "Method not supported: " + method, null));
    }

    @GetMapping("/api/v1/mcp/tools")
    public ResponseEntity<List<McpToolDefinition>> listTools() {
        return ResponseEntity.ok(financeMcpTools.getToolDefinitions());
    }

    @PostMapping("/api/v1/mcp/call")
    public ResponseEntity<McpToolResult> callTool(@RequestBody McpToolCall toolCall) {
        McpToolResult result = financeMcpTools.executeTool(toolCall.getName(), toolCall.getArguments());
        return ResponseEntity.ok(result);
    }
}
