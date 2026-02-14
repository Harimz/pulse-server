package com.pm.pulseserver.modules.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AppAuthProperties(
        String jwtSecret,
        int accessMinutes,
        int refreshDays,
        String refreshCookieName,
        boolean cookieSecure,
        String cookieSameSite
) {
}
