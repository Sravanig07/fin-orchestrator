package com.finorchestrator.mcp.notification;

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
public class NotificationMcpTools {

    private static final Logger log = LoggerFactory.getLogger(NotificationMcpTools.class);

    private final RestTemplate restTemplate;

    @Value("${services.notification-service:http://localhost:8086}")
    private String notificationServiceUrl;

    public List<McpToolDefinition> getToolDefinitions() {
        return List.of(
                McpToolDefinition.builder()
                        .name("createReminder")
                        .description("Creates a calendar reminder or financial notification for upcoming payments or contract renewals")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "title", Map.of("type", "string", "description", "Reminder title"),
                                        "message", Map.of("type", "string", "description", "Reminder details"),
                                        "priority", Map.of("type", "string", "description", "LOW, NORMAL, HIGH, URGENT")
                                ),
                                "required", List.of("userId", "title", "message")
                        )).build(),

                McpToolDefinition.builder()
                        .name("listReminders")
                        .description("Lists active reminders and notifications for the user")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of("userId", Map.of("type", "integer", "description", "User ID")),
                                "required", List.of("userId")
                        )).build(),

                McpToolDefinition.builder()
                        .name("cancelReminder")
                        .description("Cancels or removes an active reminder by ID")
                        .inputSchema(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "userId", Map.of("type", "integer", "description", "User ID"),
                                        "reminderId", Map.of("type", "integer", "description", "Reminder ID")
                                ),
                                "required", List.of("userId", "reminderId")
                        )).build()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> args) {
        log.info("Executing Notification MCP Tool [{}] with args: {}", toolName, args);
        Long userId = args.get("userId") != null ? Long.parseLong(args.get("userId").toString()) : 1L;

        try {
            return switch (toolName) {
                case "createReminder" -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("title", args.get("title"));
                    body.put("message", args.get("message"));
                    body.put("priority", args.getOrDefault("priority", "NORMAL"));
                    String url = notificationServiceUrl + "/api/v1/notifications/reminders";
                    var resp = restTemplate.postForObject(url, body, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyMap());
                }
                case "listReminders" -> {
                    String url = notificationServiceUrl + "/api/v1/notifications";
                    var resp = restTemplate.getForObject(url, ApiResponse.class);
                    yield McpToolResult.success(resp != null ? resp.getData() : Collections.emptyList());
                }
                case "cancelReminder" -> {
                    Long rid = Long.parseLong(args.get("reminderId").toString());
                    String url = String.format("%s/api/v1/notifications/reminders/%d", notificationServiceUrl, rid);
                    restTemplate.delete(url);
                    yield McpToolResult.success(Map.of("status", "CANCELLED", "reminderId", rid));
                }
                default -> McpToolResult.error("Unknown notification tool: " + toolName);
            };
        } catch (Exception e) {
            log.error("Failed executing notification tool [{}]: {}", toolName, e.getMessage());
            return McpToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
}
