package com.finorchestrator.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.common.event.DomainEvent;
import com.finorchestrator.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "finorchestrator.risk.v1", groupId = "notification-service-group")
    public void consumeRiskEvent(String message) {
        try {
            log.info("Notification Service received risk event: {}", message);
            DomainEvent<?> event = objectMapper.readValue(message, DomainEvent.class);
            Map<String, Object> payload = (Map<String, Object>) event.getPayload();

            Long userId = Long.parseLong(payload.get("userId").toString());
            String severity = payload.get("severity") != null ? payload.get("severity").toString() : "HIGH";
            String description = payload.get("description") != null ? payload.get("description").toString() : "Risk alert detected";

            notificationService.createNotification(userId, "Risk Alert: " + severity, description, "RISK_ALERT", severity);
        } catch (Exception e) {
            log.warn("Failed to process risk event in notification listener: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "finorchestrator.contracts.v1", groupId = "notification-service-group")
    public void consumeContractEvent(String message) {
        try {
            DomainEvent<?> event = objectMapper.readValue(message, DomainEvent.class);
            if ("CONTRACT_EXPIRING".equals(event.getEventType())) {
                Map<String, Object> payload = (Map<String, Object>) event.getPayload();
                Long userId = Long.parseLong(payload.get("userId").toString());
                String title = payload.get("title").toString();
                notificationService.createNotification(userId, "Contract Expiring Soon", "Contract [" + title + "] is expiring.", "CONTRACT_EXPIRY", "HIGH");
            }
        } catch (Exception e) {
            log.warn("Failed to process contract event in notification listener: {}", e.getMessage());
        }
    }
}
