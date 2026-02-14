package com.pm.pulseserver.modules.users.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @Size(min = 1, max = 80) String displayName,
        @Size(max = 280) String bio,
        @Size(max = 2048) String avatarUrl
) {
}
