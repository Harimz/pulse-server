package com.pm.pulseserver.modules.users.api;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.follows.app.FollowService;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.modules.posts.app.PostService;
import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import com.pm.pulseserver.modules.users.api.dto.UpdateMyProfileRequest;
import com.pm.pulseserver.modules.users.app.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;

    @GetMapping("/{username}")
    public PublicUserProfileResponse getPublicProfile(@PathVariable String username) {
        return userService.getPublicUserProfile(username);
    }

    @GetMapping("/me/profile")
    public PublicUserProfileResponse getMyProfile(@AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal) {
        UUID userId = UUID.fromString(principal.userId());

        return userService.getMyPublicUserProfile(userId);
    }

    @PatchMapping("/me/profile")
    public PublicUserProfileResponse updateMyProfile(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @Valid @RequestBody UpdateMyProfileRequest req
    ) {
        UUID userId = UUID.fromString(principal.userId());
        return userService.updateMyProfile(userId, principal.username(), req);
    }

    @GetMapping("/{username}/posts")
    public CursorPageResponse<PostResponse> getUserPosts(
            @PathVariable String username,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return postService.getUserPostsByUsername(username, cursor, limit);
    }

    @PostMapping("/{username}/follow")
    public void follow(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable String username
    ) {
        UUID me = UUID.fromString(principal.userId());
        followService.follow(me, username);
    }

    @DeleteMapping("/{username}/follow")
    public void unfollow(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable String username
            ) {
        UUID me = UUID.fromString(principal.userId());
        followService.unfollow(me, username);
    }

    @GetMapping("/{username}/follow-counts")
    public FollowService.FollowCounts getFollowCounts(@PathVariable String username) {
        return followService.getCounts(username);
    }
}
