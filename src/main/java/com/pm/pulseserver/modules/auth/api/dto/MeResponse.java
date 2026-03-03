package com.pm.pulseserver.modules.auth.api.dto;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String username,
        String email,
        Profile profile
) {

    public record Profile (
            String displayName,
            String bio,
            String avatarUrl
    ) {}
}
