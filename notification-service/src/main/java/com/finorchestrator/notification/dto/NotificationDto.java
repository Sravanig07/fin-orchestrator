package com.finorchestrator.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type; // REMINDER, RISK_ALERT, CONTRACT_EXPIRY, PAYMENT_DUE
    private String priority;
    private Boolean isRead;
    private Instant readAt;
    private Instant triggerTime;
    private Instant createdAt;
}
