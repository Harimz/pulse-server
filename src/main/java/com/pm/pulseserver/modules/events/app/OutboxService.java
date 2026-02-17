package com.pm.pulseserver.modules.events.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.pulseserver.modules.events.domain.OutboxEvent;
import com.pm.pulseserver.modules.events.domain.OutboxStatus;
import com.pm.pulseserver.modules.events.infra.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void enqueue(String eventType, String aggregateType, UUID aggregateId, Map<String, Object> payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }

        outboxRepository.save(OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payloadJson(json)
                .status(OutboxStatus.PENDING)
                .availableAt(Instant.now())
                .attempts(0)
                .build());
    }
}
