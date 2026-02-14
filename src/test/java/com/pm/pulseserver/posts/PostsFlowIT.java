package com.pm.pulseserver.posts;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.modules.posts.api.dto.CreatePostRequest;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import static org.assertj.core.api.Assertions.assertThat;

public class PostsFlowIT extends IntegrationTestBase {

    @Autowired
    private RestTestClient http;

    @Test
    void logged_in_user_can_create_post() {
        var auth = registerAndGetAuth("u");

        http.post()
                .uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + auth.accessToken())
                .body(new CreatePostRequest("This is a test post"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PostResponse.class)
                .value(p -> {
                    assertThat(p).isNotNull();
                    assertThat(p.id()).isNotNull();
                    assertThat(p.authorId()).isNotNull();
                    assertThat(p.body()).isEqualTo("This is a test post");
                    assertThat(p.createdAt()).isNotNull();
                });
    }

    @Test
    void anonymous_user_cannot_create_post() {
        http.post()
                .uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreatePostRequest("This is a test post"))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void anyone_can_get_post_by_id() {
        var auth = registerAndGetAuth("u");

        var created =         http.post()
                .uri("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + auth.accessToken())
                .body(new CreatePostRequest("This is a test post"))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PostResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();

        http.get()
                .uri("/api/v1/posts/{id}", created.id())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(PostResponse.class)
                .value(p -> {
                    assertThat(p).isNotNull();
                    assertThat(p.id()).isEqualTo(created.id());
                    assertThat(p.body()).isEqualTo("This is a test post");
                });
    }

    @Test
    void public_can_get_users_posts_first_page() {
        String username = uniqueUsername("u");
        var auth = registerAndGetAuth(username);
        createPost(auth, "p1 - asdahuaihsd");
        createPost(auth, "p2 - ahijasiduhasd");
        createPost(auth, "p3 - oasdiuyaiusdgh");

        var page = http.get()
                .uri("/api/v1/users/" + username + "/posts?limit=2")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CursorPageResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(page).isNotNull();
        assertThat(page.items()).isNotNull();
        assertThat(page.items().size()).isEqualTo(2);
        assertThat(page.nextCursor()).isNotNull();
    }

    @Test
    void public_user_posts_support_cursor_pagination() {
        String username = uniqueUsername("u");
        var auth = registerAndGetAuth(username);

        createPost(auth, "p1");
        createPost(auth, "p2");
        createPost(auth, "p3");
        createPost(auth, "p4");
        createPost(auth, "p5");

        var page1 = http.get()
                .uri("/api/v1/users/" + username + "/posts?limit=2")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CursorPageResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(page1).isNotNull();
        assertThat(page1.items().size()).isEqualTo(2);
        assertThat(page1.nextCursor()).isNotBlank();

        var page2 = http.get()
                .uri("/api/v1/users/" + username + "/posts?limit=2&cursor=" + page1.nextCursor())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CursorPageResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(page2).isNotNull();
        assertThat(page2.items().size()).isEqualTo(2);

        var firstIdPage1 = ((java.util.LinkedHashMap<?, ?>) page1.items().get(0)).get("id").toString();
        var firstIdPage2 = ((java.util.LinkedHashMap<?, ?>) page2.items().get(0)).get("id").toString();
        assertThat(firstIdPage2).isNotEqualTo(firstIdPage1);
    }

    private AuthResponse registerAndGetAuth(String prefixOrUsername) {
        String username = prefixOrUsername.contains("_") ? prefixOrUsername : uniqueUsername(prefixOrUsername);

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
