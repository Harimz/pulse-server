package com.pm.pulseserver.modules.events.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.pulseserver.common.cache.CacheService;
import com.pm.pulseserver.modules.events.domain.OutboxEvent;
import com.pm.pulseserver.modules.notifications.domain.Notification;
import com.pm.pulseserver.modules.notifications.infra.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFollowedHandler {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final CacheService cacheService;

    @Transactional
    public void handle(OutboxEvent event) {
        try {
            JsonNode node = objectMapper.readTree(event.getPayloadJson());
            UUID fromUserId = UUID.fromString(node.get("fromUserId").asText());
            UUID toUserId = UUID.fromString(node.get("toUserId").asText());

            notificationRepository.save(Notification.builder()
                    .id(UUID.randomUUID())
                    .toUserId(toUserId)
                    .fromUserId(fromUserId)
                    .type("USER_FOLLOWED")
                    .payloadJson("{}")
                    .readAt(null)
                    .build());

            String key = "notif:unread:" + toUserId;

            cacheService.increment(key);
        } catch (DataIntegrityViolationException e) {
        } catch (Exception e) {
            throw new RuntimeException("Failed handling USER_FOLLOWED");
        }
    }
}
