package com.pm.pulseserver.modules.posts.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        Author author,
        String body,
        Instant createdAt,
        List<String> mentions,
        long likesCount,
        long commentCount,
        boolean likedByMe
) {
    public record Author (
            UUID id,
            String username,
            String displayName,
            String avatar
    ) {}
}
