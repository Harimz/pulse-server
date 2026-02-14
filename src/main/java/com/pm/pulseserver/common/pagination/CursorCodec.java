package com.pm.pulseserver.common.pagination;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class CursorCodec {

    public record PostCursor(Instant createdAt, UUID id) {}

    public static String encode(Instant createdAt, UUID id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static PostCursor decode(String cursor) {
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid cursor");
        return new PostCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
    }
}
