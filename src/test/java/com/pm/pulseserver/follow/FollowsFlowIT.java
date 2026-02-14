package com.pm.pulseserver.follow;

import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;
import static org.assertj.core.api.Assertions.assertThat;

public class FollowsFlowIT extends IntegrationTestBase {

    @Autowired
    private RestTestClient http;

    @Test
    void logged_in_user_can_follow_and_unfollow() {
        String a = uniqueUsername("a");
        String b = uniqueUsername("b");

        var authA = registerAndGetAuth(a);
        registerAndGetAuth(b);

        http.post()
                .uri("/api/v1/users/" + b + "/follow")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful();

        http.post()
                .uri("/api/v1/users/" + b + "/follow")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful();

        http.delete()
                .uri("/api/v1/users/" + b + "/follow")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful();

        http.delete()
                .uri("/api/v1/users/" + b + "/follow")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void anonymous_user_cannot_follow() {
        String a = uniqueUsername("a");
        registerAndGetAuth(a);

        http.post()
                .uri("/api/v1/users/" + a + "/follow")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    private AuthResponse registerAndGetAuth(String username) {
        RegisterRequest req = new RegisterRequest(
                username,
                uniqueEmail(username),
                "password123",
                "Test Name"
        );

        var reg = http.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult();

        AuthResponse auth = reg.getResponseBody();
        assertThat(auth).isNotNull();
        assertThat(auth.accessToken()).isNotBlank();
        return auth;
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueEmail(String username) {
        return username + "@example.com";
    }
}
