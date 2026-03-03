package com.pm.pulseserver.modules.comment.infra;

import com.pm.pulseserver.modules.comment.domain.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    long countByPostId(UUID postId);

    @Query(value = """
        SELECT post_id, COUNT(*) AS cnt
        FROM post_comments
        WHERE post_id IN (:postIds)
        GROUP BY post_id
        """, nativeQuery = true)
    List<Object[]> countByPostIds(@Param("postIds") List<UUID> postIds);

    @Query(value = """
        SELECT *
        FROM post_comments c
        WHERE c.post_id = :postId
        ORDER BY c.created_at DESC, c.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<PostComment> findFirstPage(@Param("postId") UUID postId, @Param("limit") int limit);

    @Query(value = """
        SELECT *
        FROM post_comments c
        WHERE c.post_id = :postId
          AND (c.created_at, c.id) < (:cursorCreatedAt, :cursorId)
        ORDER BY c.created_at DESC, c.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<PostComment> findAfterCursor(
            @Param("postId") UUID postId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );}
