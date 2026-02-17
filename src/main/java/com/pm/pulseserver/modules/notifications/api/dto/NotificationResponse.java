package com.pm.pulseserver.modules.notifications.api.dto;

import java.time.Instant;
import java.util.UUID;


public record NotificationResponse(
        UUID id,
        UUID toUserId,
        String type,
        String payloadJson,
        Instant createdAt,
        Instant readAt,
        FromUser fromUser
) {
    public record FromUser(
            UUID id,
            String username,
            String avatarUrl
    ) {}
}