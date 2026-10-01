package com.finorchestrator.common.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolResult {
    private Object content;
    @Builder.Default
    private boolean isError = false;
    private Map<String, Object> metadata;

    public static McpToolResult success(Object content) {
        return McpToolResult.builder()
                .content(content)
                .isError(false)
                .build();
    }

    public static McpToolResult success(Object content, Map<String, Object> metadata) {
        return McpToolResult.builder()
                .content(content)
                .isError(false)
                .metadata(metadata)
                .build();
    }

    public static McpToolResult error(String errorMessage) {
        return McpToolResult.builder()
                .content(errorMessage)
                .isError(true)
                .build();
    }
}
