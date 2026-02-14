package com.pm.pulseserver.modules.posts.infra;

import com.pm.pulseserver.modules.posts.domain.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query(value = """
        SELECT *
        FROM posts p
        WHERE p.author_id = :authorId
          AND (
                :cursorCreatedAt IS NULL
                OR (p.created_at, p.id) < (:cursorCreatedAt, :cursorId)
              )
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Post> findUserPostsPage(
            @Param("authorId") UUID authorId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );
}
