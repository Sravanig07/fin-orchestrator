package com.finorchestrator.risk.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finorchestrator.common.event.DomainEvent;
import com.finorchestrator.common.event.TransactionCreatedEvent;
import com.finorchestrator.risk.service.RiskEngineService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransactionConsumer.class);

    private final RiskEngineService riskEngineService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "finorchestrator.transactions.v1", groupId = "risk-service-group")
    public void consumeTransactionCreated(String message) {
        try {
            log.info("Risk Service consumed transaction event: {}", message);
            DomainEvent<?> event = objectMapper.readValue(message, DomainEvent.class);
            if ("TRANSACTION_CREATED".equals(event.getEventType())) {
                TransactionCreatedEvent tx = objectMapper.convertValue(event.getPayload(), TransactionCreatedEvent.class);
                riskEngineService.analyzeTransaction(tx.getUserId(), tx.getTransactionId(), tx.getAmount());
            }
        } catch (Exception e) {
            log.warn("Error processing Kafka transaction event in risk-service: {}", e.getMessage());
        }
    }
}
