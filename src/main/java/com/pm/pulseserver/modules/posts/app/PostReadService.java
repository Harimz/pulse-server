package com.pm.pulseserver.modules.posts.app;

import com.pm.pulseserver.modules.comment.infra.PostCommentRepository;
import com.pm.pulseserver.modules.likes.infra.PostLikeRepository;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.modules.posts.domain.Post;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostReadService {

    private final UserRepository userRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;

    public List<PostResponse> enrich(List<Post> posts, UUID me) {
        if (posts.isEmpty()) return List.of();

        var authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        var users = userRepository.findAllByIdIn(authorIds);
        var byAuthorId = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        var postIds = posts.stream().map(Post::getId).toList();

        var likeCounts = toCountMap(likeRepository.countByPostIds(postIds));
        var commentCounts = toCountMap(commentRepository.countByPostIds(postIds));

        final Set<UUID> likedByMe =
                (me == null)
                        ? Set.of()
                        : new HashSet<>(likeRepository.findLikedPostIds(me, postIds));

        return posts.stream().map(p -> {

            var u = byAuthorId.get(p.getAuthorId());

            PostResponse.Author author = (u == null) ? null : new PostResponse.Author(
                    u.getId(),
                    u.getUsername(),
                    u.getProfile() == null ? null : u.getProfile().getDisplayName(),
                    u.getProfile() == null ? null : u.getProfile().getAvatarUrl()
            );

            long likes = likeCounts.getOrDefault(p.getId(), 0L);
            long comments = commentCounts.getOrDefault(p.getId(), 0L);

            boolean isLiked = likedByMe.contains(p.getId());

            return new PostResponse(
                    p.getId(),
                    author,
                    p.getBody(),
                    p.getCreatedAt(),
                    List.of(),
                    likes,
                    comments,
                    isLiked
            );
        }).toList();
    }

    private Map<UUID, Long> toCountMap(List<Object[]> rows) {
        Map<UUID, Long> m = new HashMap<>();
        for (Object[] r : rows) {
            UUID postId = (UUID) r[0];
            Number cnt = (Number) r[1];
            m.put(postId, cnt.longValue());
        }
        return m;
    }
}
