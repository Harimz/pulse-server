package com.pm.pulseserver.modules.feeds.api;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.feeds.app.FeedQueryService;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final FeedQueryService feedQueryService;

    @GetMapping("/explore")
    public CursorPageResponse<PostResponse> explore(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return feedQueryService.getExplore(cursor, limit);
    }

    @GetMapping("/following")
    public CursorPageResponse<PostResponse> following(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UUID me = UUID.fromString(principal.userId());
        return feedQueryService.getFollowing(me, cursor, limit);
    }
}




