package com.pm.pulseserver.users;

import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.modules.users.api.dto.UpdateMyProfileRequest;
import com.pm.pulseserver.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import static org.assertj.core.api.Assertions.assertThat;

public class UsersFlowIT extends IntegrationTestBase {

    @Autowired
    private RestTestClient http;

    @Test
    void update_my_profile_updates_public_profile_and_requires_auth() {
        String username = uniqueUsername("u");
        RegisterRequest newUserReq = new RegisterRequest(
                username,
                uniqueEmail(username),
                "password123",
                "Test Name"
        );

        var reg = http.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newUserReq)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(AuthResponse.class)
                .returnResult();

        AuthResponse auth = reg.getResponseBody();
        assertThat(auth).isNotNull();
        assertThat(auth.accessToken()).isNotBlank();

        http.get()
                .uri("/api/v1/users/" + username)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username)
                .jsonPath("$.displayName").isEqualTo("Test Name");

        String updatedDisplayName = "Updated Display Name";
        String updatedBio = "Updated Bio";
        String updatedAvatar = "https://example.com/avatar.png";

        UpdateMyProfileRequest updateProfileReq = new UpdateMyProfileRequest(
                updatedDisplayName,
                updatedBio,
                updatedAvatar
        );

        http.patch()
                .uri("/api/v1/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.accessToken())
                .body(updateProfileReq)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username)
                .jsonPath("$.displayName").isEqualTo(updatedDisplayName)
                .jsonPath("$.bio").isEqualTo(updatedBio)
                .jsonPath("$.avatarUrl").isEqualTo(updatedAvatar);

        http.get()
                .uri("/api/v1/users/" + username)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.displayName").isEqualTo(updatedDisplayName)
                .jsonPath("$.bio").isEqualTo(updatedBio)
                .jsonPath("$.avatarUrl").isEqualTo(updatedAvatar);
    }

    @Test
    void update_my_profile_is_401_without_token() {
        http.patch()
                .uri("/api/v1/users/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateMyProfileRequest("X", "Y", "Z"))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueEmail(String username) {
        return username + "@example.com";
    }
}
