package com.pm.pulseserver.modules.users.api.dto;

import java.util.UUID;

public record PublicUserProfileResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String avatarUrl
) {}