package com.finorchestrator.common.exception;

import org.springframework.http.HttpStatus;

public class ToolExecutionException extends ApiException {
    private final String toolName;

    public ToolExecutionException(String toolName, String message) {
        super(String.format("Error executing MCP tool [%s]: %s", toolName, message),
                HttpStatus.INTERNAL_SERVER_ERROR, "TOOL_EXECUTION_FAILURE");
        this.toolName = toolName;
    }

    public String getToolName() {
        return toolName;
    }
}
