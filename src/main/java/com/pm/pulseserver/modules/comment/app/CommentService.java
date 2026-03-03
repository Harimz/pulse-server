package com.pm.pulseserver.modules.comment.app;

import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.common.pagination.CursorCodec;
import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.comment.api.dto.CommentResponse;
import com.pm.pulseserver.modules.comment.domain.PostComment;
import com.pm.pulseserver.modules.comment.infra.PostCommentRepository;
import com.pm.pulseserver.modules.posts.infra.PostRepository;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse add(UUID me, UUID postId, String body) {
        if (!postRepository.existsById(postId)) throw new NotFoundException("Post not found");

        PostComment c = PostComment.builder()
                .id(UUID.randomUUID())
                .postId(postId)
                .userId(me)
                .body(body)
                .build();

        var saved = commentRepository.saveAndFlush(c);

        User u = userRepository.findWithProfileById(me)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toDto(saved, u);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> list(UUID postId, String cursor, int limit) {
        if (!postRepository.existsById(postId)) throw new NotFoundException("Post not found");

        limit = Math.min(Math.max(limit, 1), 50);

        CursorCodec.PostCursor decoded = (cursor == null || cursor.isBlank()) ? null : CursorCodec.decode(cursor);

        var rows = (decoded == null)
                ? commentRepository.findFirstPage(postId, limit)
                : commentRepository.findAfterCursor(postId, decoded.createdAt(), decoded.id(), limit);

        var authorIds = rows.stream().map(PostComment::getUserId).distinct().toList();
        var users = authorIds.isEmpty() ? List.<User>of() : userRepository.findAllByIdIn(authorIds);
        var byId = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        var items = rows.stream()
                .map(c -> toDto(c, byId.get(c.getUserId())))
                .toList();

        String nextCursor = null;
        if (rows.size() == limit) {
            var last = rows.get(rows.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    @Transactional
    public void delete(UUID me, UUID commentId) {
        PostComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!c.getUserId().equals(me)) throw new NotFoundException("Comment not found");

        commentRepository.delete(c);
    }

    private CommentResponse toDto(PostComment c, User u) {
        CommentResponse.Author author = null;
        if (u != null) {
            author = new CommentResponse.Author(
                    u.getId(),
                    u.getUsername(),
                    u.getProfile() == null ? null : u.getProfile().getAvatarUrl()
            );
        }

        return new CommentResponse(
                c.getId(),
                c.getPostId(),
                author,
                c.getBody(),
                c.getCreatedAt()
        );
    }
}
