package com.finorchestrator.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent<T> {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    @Builder.Default
    private String eventVersion = "1.0";
    @Builder.Default
    private String correlationId = MDC.get("correlationId");
    @Builder.Default
    private Instant timestamp = Instant.now();
    private String producerService;
    private T payload;

    public static <T> DomainEvent<T> of(String eventType, String producerService, T payload) {
        return DomainEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventVersion("1.0")
                .correlationId(MDC.get("correlationId"))
                .timestamp(Instant.now())
                .producerService(producerService)
                .payload(payload)
                .build();
    }
}
