package com.pm.pulseserver.feeds;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.modules.posts.api.dto.CreatePostRequest;
import com.pm.pulseserver.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedsFlowIT extends IntegrationTestBase {

    @Autowired
    private RestTestClient http;

    @Test
    void explore_feed_is_public_and_returns_posts() {
        var a = uniqueUsername("a");
        var authA = registerAndGetAuth(a);

        createPost(authA, "hello explore 1");
        createPost(authA, "hello explore 2");

        var page = http.get()
                .uri("/api/v1/feed/explore?limit=10")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CursorPageResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(page).isNotNull();
        assertThat(page.items()).isNotNull();
        assertThat(page.items().size()).isGreaterThanOrEqualTo(2);
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

    @Test
    void following_feed_required_auth() {
        http.get()
                .uri("/api/v1/feed/following")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void following_feed_returns_only_followed_users_posts() {
        var a = uniqueUsername("a");
        var b = uniqueUsername("b");
        var c = uniqueUsername("c");

        var authA = registerAndGetAuth(a);
        var authB = registerAndGetAuth(b);
        var authC = registerAndGetAuth(c);

        createPost(authB, "from B 1");
        createPost(authC, "from C 1");

        http.post()
                .uri("/api/v1/users/" + b + "/follow")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful();

        var page = http.get()
                .uri("/api/v1/feed/following?limit=20")
                .header("Authorization", "Bearer " + authA.accessToken())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CursorPageResponse.class)
                .returnResult()
                .getResponseBody();


        assertThat(page).isNotNull();
        assertThat(page.items()).isNotNull();

        var containsB = page.items().stream().anyMatch(it ->
                ((java.util.LinkedHashMap<?, ?>) it).get("body").toString().startsWith("from B")
        );
        var containsC = page.items().stream().anyMatch(it ->
                ((java.util.LinkedHashMap<?, ?>) it).get("body").toString().startsWith("from C")
        );

        assertThat(containsB).isTrue();
        assertThat(containsC).isFalse();
    }

    private void createPost(AuthResponse auth, String body) {
        http.post()
                .uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + auth.accessToken())
                .body(new CreatePostRequest(body))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueEmail(String username) {
        return username + "@example.com";
    }
}
