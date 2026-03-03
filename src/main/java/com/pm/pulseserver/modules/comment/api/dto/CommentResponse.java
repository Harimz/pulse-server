package com.pm.pulseserver.modules.comment.api.dto;


import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        Author author,
        String body,
        Instant createdAt
) {
    public record Author(UUID id, String username, String avatarUrl) {}
}