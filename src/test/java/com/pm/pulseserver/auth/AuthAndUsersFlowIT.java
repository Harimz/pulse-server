package com.pm.pulseserver.auth;

import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthAndUsersFlowIT extends IntegrationTestBase {

    @Autowired
    private RestTestClient http;

    @Test
    void register_then_getPublicProfile() {
        String username = uniqueUsername("u");
        RegisterRequest req = new RegisterRequest(
                username,
                uniqueEmail(username),
                "password123",
                "Test Name"
        );

        var reg = http.post()
                .uri("/api/v1/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().exists(HttpHeaders.SET_COOKIE)
                .expectBody(AuthResponse.class)
                .returnResult();

        AuthResponse body = reg.getResponseBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isNotBlank();

        var setCookies = reg.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).isNotNull();
        assertThat(setCookies.toString()).contains("pulse_sid=");
        assertThat(setCookies.toString()).contains("pulse_refresh=");

        http.get()
                .uri("/api/v1/users/" + username)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username)
                .jsonPath("$.displayName").isEqualTo("Test Name");
    }

    @Test
    void refresh_rotates_session_and_returns_new_access_token() {
        String username = uniqueUsername("u");
        RegisterRequest req = new RegisterRequest(
                username,
                uniqueEmail(username),
                "password123",
                "Test Name"
        );

        var reg = http.post()
                .uri("/api/v1/auth/register")
                .body(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult();

        String oldAccess = reg.getResponseBody().accessToken();
        var regCookies = reg.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(regCookies).isNotNull();

        var refreshRes = http.post()
                .uri("/api/v1/auth/refresh")
                .header(HttpHeaders.COOKIE, cookieHeader(regCookies))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult();

        String newAccess = refreshRes.getResponseBody().accessToken();
        assertThat(newAccess).isNotBlank();
        assertThat(newAccess).isNotEqualTo(oldAccess);

        var refreshedCookies = refreshRes.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(refreshedCookies).isNotNull();
        assertThat(refreshedCookies.toString()).contains("pulse_sid=");
        assertThat(refreshedCookies.toString()).contains("pulse_refresh=");
    }

    @Test
    void logout_revokes_session_and_clears_cookiew() {
        String username = uniqueUsername("u");
        RegisterRequest req = new RegisterRequest(
                username,
                uniqueEmail(username),
                "password123",
                "Test Name"
        );

        var reg = http.post()
                .uri("/api/v1/auth/register")
                .body(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult();

        var regCookies = reg.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(regCookies).isNotNull();

        var logoutRes = http.post()
                .uri("/api/v1/auth/logout")
                .header(HttpHeaders.COOKIE, cookieHeader(regCookies))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult();

        var logoutCookies = logoutRes.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(logoutCookies).isNotNull();

        assertThat(logoutCookies.toString()).contains("Max-Age=0");

        http.post()
                .uri("/api/v1/auth/refresh")
                .header(HttpHeaders.COOKIE, cookieHeader(regCookies))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    private static String cookieValueFromSetCookie(java.util.List<String> setCookies, String cookieName) {
        return setCookies.stream()
                .filter(c -> c.startsWith(cookieName + "="))
                .findFirst()
                .map(c -> c.split(";", 2)[0])
                .orElseThrow(() -> new IllegalStateException("Missing cookie: " + cookieName));
    }

    private static String cookieHeader(java.util.List<String> setCookies) {
        String sid = cookieValueFromSetCookie(setCookies, "pulse_sid");
        String refresh = cookieValueFromSetCookie(setCookies, "pulse_refresh");
        return sid + "; " + refresh;
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueEmail(String username) {
        return username + "@example.com";
    }
}
