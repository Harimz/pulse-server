package com.pm.pulseserver.modules.follows.infra;

import com.pm.pulseserver.modules.follows.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Modifying
    @Query("delete from Follow f where f.followerId = :followerId and f.followingId = :followingId")
    int deleteByPair(@Param("followerId") UUID followerId, @Param("followingId") UUID followingId);

    long countByFollowerId(UUID followerId);   // how many people I follow
    long countByFollowingId(UUID followingId); // how many followers I have
}
