package com.pm.pulseserver.modules.feeds.app;

import com.pm.pulseserver.common.cache.CacheService;
import com.pm.pulseserver.common.pagination.CursorCodec;
import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
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
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getExplore(String cursor, int limit) {

        limit = clampLimit(limit);

        String key = "feed:explore:" +
                (cursor == null ? "first" : cursor) +
                ":" + limit;

        var cached = cacheService.get(key, CursorPageResponse.class);
        if (cached != null) {
            return cached;
        }

        CursorCodec.PostCursor decoded = decodeCursor(cursor);

        var posts = (decoded == null)
                ? postRepository.findExploreFirstPage(limit)
                : postRepository.findExploreAfterCursor(decoded.createdAt(), decoded.id(), limit);

        var result = toPage(posts, limit);

        cacheService.set(key, result);

        return result;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getFollowing(UUID me, String cursor, int limit) {
        limit = clampLimit(limit);

        CursorCodec.PostCursor decoded = decodeCursor(cursor);

        var posts = (decoded == null)
                ? postRepository.findFollowingFirstPage(me, limit)
                : postRepository.findFollowingAfterCursor(me, decoded.createdAt(), decoded.id(), limit);

        return toPage(posts, limit);
    }

    private CursorPageResponse<PostResponse> toPage(List<Post> posts, int limit) {
        var items = posts.stream()
                .map(p -> new PostResponse(
                        p.getId(),
                        p.getAuthorId(),
                        p.getBody(),
                        p.getCreatedAt(),
                        List.of()
                ))
                .toList();

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
