package com.pm.pulseserver.modules.posts.infra;

import com.pm.pulseserver.modules.posts.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query(value = """
        SELECT *
        FROM posts p
        WHERE p.author_id = :authorId
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> findUserPostsFirstPage(
            @Param("authorId") UUID authorId,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT *
        FROM posts p
        WHERE p.author_id = :authorId
          AND (p.created_at, p.id) < (:cursorCreatedAt, :cursorId)
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> findUserPostsAfterCursor(
            @Param("authorId") UUID authorId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT *
    FROM posts p
    ORDER BY p.created_at DESC, p.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Post> findExploreFirstPage(@Param("limit") int limit);

    @Query(value = """
    SELECT *
    FROM posts p
    WHERE (p.created_at, p.id) < (:cursorCreatedAt, :cursorId)
    ORDER BY p.created_at DESC, p.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Post> findExploreAfterCursor(
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT p.*
    FROM posts p
    JOIN follows f ON f.following_id = p.author_id
    WHERE f.follower_id = :me
    ORDER BY p.created_at DESC, p.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Post> findFollowingFirstPage(
            @Param("me") UUID me,
            @Param("limit") int limit
    );

    @Query(value = """
    SELECT p.*
    FROM posts p
    JOIN follows f ON f.following_id = p.author_id
    WHERE f.follower_id = :me
      AND (p.created_at, p.id) < (:cursorCreatedAt, :cursorId)
    ORDER BY p.created_at DESC, p.id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Post> findFollowingAfterCursor(
            @Param("me") UUID me,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );
}
