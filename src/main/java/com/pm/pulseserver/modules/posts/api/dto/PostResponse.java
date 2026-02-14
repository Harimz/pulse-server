package com.pm.pulseserver.modules.posts.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String body,
        Instant createdAt,
        List<String> mentions
) {
}
