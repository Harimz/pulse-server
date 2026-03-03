package com.pm.pulseserver.modules.posts.api;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.comment.api.dto.CommentResponse;
import com.pm.pulseserver.modules.comment.api.dto.CreateCommentRequest;
import com.pm.pulseserver.modules.comment.app.CommentService;
import com.pm.pulseserver.modules.likes.app.LikeService;
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
    private final LikeService likeService;
    private final CommentService commentService;

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

    @PostMapping("/{id}/like")
    public void like(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID me = UUID.fromString(principal.userId());
        likeService.like(me, id);
    }

    @DeleteMapping("/{id}/like")
    public void unlike(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID me = UUID.fromString(principal.userId());
        likeService.unlike(me, id);
    }

    @PostMapping("/{id}/comments")
    public CommentResponse add(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest req
    ) {
        UUID me = UUID.fromString(principal.userId());
        return commentService.add(me, id, req.body());
    }

    @GetMapping("/{id}/comments")
    public CursorPageResponse<CommentResponse> list(
            @PathVariable UUID id,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return commentService.list(id, cursor, limit);
    }
}
