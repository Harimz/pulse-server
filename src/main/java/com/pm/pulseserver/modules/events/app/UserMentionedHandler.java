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
public class UserMentionedHandler {

    public final ObjectMapper objectMapper;
    public final NotificationRepository notificationRepository;
    private final CacheService cacheService;

    @Transactional
    public void handle(OutboxEvent event) {
        try {
            JsonNode node = objectMapper.readTree(event.getPayloadJson());

            UUID postId = UUID.fromString(node.get("postId").asText());
            UUID fromUserId = UUID.fromString(node.get("fromUserId").asText());
            UUID toUserId = UUID.fromString(node.get("toUserId").asText());

            notificationRepository.save(Notification.builder()
                    .id(UUID.randomUUID())
                    .toUserId(toUserId)
                    .fromUserId(fromUserId)
                    .type("USER_MENTIONED")
                    .payloadJson("{\"postId\":\"" + postId + "\"}")
                    .readAt(null)
                    .build());

            String key = "notif:unread:" + toUserId;
            cacheService.increment(key);
        } catch (DataIntegrityViolationException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("Failed handling USER_MENTIONED", e);
        }
    }

}
