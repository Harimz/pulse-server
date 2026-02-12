package com.pm.pulseserver.common.api;

import java.util.Map;

public record ApiError(
        String code,
        String message,
        Map<String, Object> details
) {
}
