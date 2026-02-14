package com.pm.pulseserver.modules.posts.api;

import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.posts.api.dto.CreatePostRequest;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.modules.posts.app.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostResponse create(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @Valid @RequestBody CreatePostRequest req
            ) {
        UUID authorId = UUID.fromString(principal.userId());
        return postService.createPost(authorId, req.body());
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable UUID id) {
        return postService.getPost(id);
    }
}
