package com.pm.pulseserver.modules.posts.app;

import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.common.pagination.CursorCodec;
import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.posts.api.dto.PostResponse;
import com.pm.pulseserver.modules.posts.domain.Post;
import com.pm.pulseserver.modules.posts.domain.PostMention;
import com.pm.pulseserver.modules.posts.infra.PostMentionRepository;
import com.pm.pulseserver.modules.posts.infra.PostRepository;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMentionRepository postMentionRepository;
    private final UserRepository userRepository;
    private final MentionService mentionService;

    @Transactional
    public PostResponse createPost(UUID authorId, String body) {
        UUID postId = UUID.randomUUID();
        Post post = Post.builder()
                .id(postId)
                .authorId(authorId)
                .body(body)
                .build();
        Post saved = postRepository.saveAndFlush(post);
        Post reloaded = postRepository.findById(saved.getId()).orElseThrow();

        var mentionResult = mentionService.extractMentionedUserIds(body);

        for (UUID mentionedId : mentionResult.userIds()) {
            if (mentionedId.equals(authorId)) continue;
            postMentionRepository.save(new PostMention(UUID.randomUUID(), reloaded.getId(), mentionedId));
        }

        return new PostResponse(
                reloaded.getId(),
                reloaded.getAuthorId(),
                reloaded.getBody(),
                reloaded.getCreatedAt(),
                mentionResult.usernames()
        );
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

        var mentions = postMentionRepository.findByPostId(postId);

        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getBody(),
                post.getCreatedAt(),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getUserPostsByUsername(String username, String cursor, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        limit = Math.min(Math.max(limit, 1), 50);

        var posts = (cursor == null || cursor.isBlank())
                ? postRepository.findUserPostsFirstPage(user.getId(), limit)
                : postRepository.findUserPostsAfterCursor(
                user.getId(),
                CursorCodec.decode(cursor).createdAt(),
                CursorCodec.decode(cursor).id(),
                limit
        );

        var items = posts.stream()
                .map(p -> new PostResponse(p.getId(), p.getAuthorId(), p.getBody(), p.getCreatedAt(), List.of()))
                .toList();

        String nextCursor = null;
        if (posts.size() == limit) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

}
