package com.pm.pulseserver.modules.auth.api;

import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.LoginRequest;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.modules.auth.app.AuthService;
import com.pm.pulseserver.modules.auth.infra.AppAuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String SESSION_COOKIE = "pulse_sid";
    private static final String REFRESH_COOKIE = "pulse_refresh";

    private final AuthService authService;
    private final AppAuthProperties props;


    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req, HttpServletResponse res) {
        var result = authService.register(req);
        setCookies(res, result.sessionId(), result.rawRefreshToken());
        return result.body();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        var result = authService.login(req);
        setCookies(res, result.sessionId(), result.rawRefreshToken());
        return result.body();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = SESSION_COOKIE, required = false) String sid,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refresh,
            HttpServletResponse res
    ) {
        if (sid == null || refresh == null) {
            throw new com.pm.pulseserver.common.exception.NotFoundException("Not authenticated");
        }

        UUID sessionId = UUID.fromString(sid);
        var result = authService.refresh(sessionId, refresh);
        setCookies(res, result.sessionId(), result.rawRefreshToken());
        return result.body();
    }

    @PostMapping("/logout")
    public void logout(
            @CookieValue(name = SESSION_COOKIE, required = false) String sid,
            HttpServletResponse res
    ) {
        if (sid != null) {
            authService.logout(UUID.fromString(sid));
        }
        clearCookies(res);
    }

    private void setCookies(HttpServletResponse res, UUID sessionId, String rawRefreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, rawRefreshToken)
                .httpOnly(true)
                .secure(props.cookieSecure())
                .path("/")
                .sameSite(props.cookieSameSite())
                .maxAge(60L * 60 * 24 * props.refreshDays())
                .build();

        ResponseCookie sidCookie = ResponseCookie.from(SESSION_COOKIE, sessionId.toString())
                .httpOnly(true)
                .secure(props.cookieSecure())
                .path("/")
                .sameSite(props.cookieSameSite())
                .maxAge(60L * 60 * 24 * props.refreshDays())
                .build();

        res.addHeader("Set-Cookie", refreshCookie.toString());
        res.addHeader("Set-Cookie", sidCookie.toString());
    }

    private void clearCookies(HttpServletResponse res) {
        res.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(props.cookieSecure()).path("/")
                .sameSite(props.cookieSameSite()).maxAge(0).build().toString());

        res.addHeader("Set-Cookie", ResponseCookie.from(SESSION_COOKIE, "")
                .httpOnly(true).secure(props.cookieSecure()).path("/")
                .sameSite(props.cookieSameSite()).maxAge(0).build().toString());
    }
}
