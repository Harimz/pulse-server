package com.pm.pulseserver.modules.likes.infra;

import com.pm.pulseserver.modules.likes.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    long countByPostId(UUID postId);

    @Query(value = """
        SELECT post_id, COUNT(*) AS cnt
        FROM post_likes
        WHERE post_id IN (:postIds)
        GROUP BY post_id
        """, nativeQuery = true)
    List<Object[]> countByPostIds(@Param("postIds") List<UUID> postIds);

    @Query(value = """
    SELECT post_id
    FROM post_likes
    WHERE user_id = :userId
      AND post_id IN (:postIds)
    """, nativeQuery = true)
    List<UUID> findLikedPostIds(
            @Param("userId") UUID userId,
            @Param("postIds") List<UUID> postIds
    );
}
