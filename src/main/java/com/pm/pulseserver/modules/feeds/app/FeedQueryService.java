package com.pm.pulseserver.modules.feeds.app;

import com.pm.pulseserver.common.pagination.CursorCodec;
import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.modules.posts.app.PostReadService;
import com.pm.pulseserver.modules.posts.domain.Post;
import com.pm.pulseserver.modules.posts.infra.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final PostRepository postRepository;
    private final PostReadService postReadService;

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getExplore(UUID me, String cursor, int limit) {
        limit = clampLimit(limit);
        CursorCodec.PostCursor decoded = decodeCursor(cursor);

        var posts = (decoded == null)
                ? postRepository.findExploreFirstPage(limit)
                : postRepository.findExploreAfterCursor(decoded.createdAt(), decoded.id(), limit);

        return toPage(posts, limit, me, "feed:explore");
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getFollowing(UUID me, String cursor, int limit) {
        limit = clampLimit(limit);
        CursorCodec.PostCursor decoded = decodeCursor(cursor);

        var posts = (decoded == null)
                ? postRepository.findFollowingFirstPage(me, limit)
                : postRepository.findFollowingAfterCursor(me, decoded.createdAt(), decoded.id(), limit);

        return toPage(posts, limit, me, "feed:following");
    }

    private CursorPageResponse<PostResponse> toPage(List<Post> posts, int limit, UUID me, String prefix) {
        var items = postReadService.enrich(posts, me);

        String nextCursor = null;
        if (posts.size() == limit) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    private static CursorCodec.PostCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        return CursorCodec.decode(cursor);
    }

    private static int clampLimit(int limit) {
        return Math.min(Math.max(limit, 1), 50);
    }
}
