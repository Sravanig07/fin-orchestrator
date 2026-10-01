package com.finorchestrator.common.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {
    @Builder.Default
    private String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private McpError error;

    public static McpResponse success(Object id, Object result) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    public static McpResponse error(Object id, int code, String message, Object data) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(new McpError(code, message, data))
                .build();
    }
}
