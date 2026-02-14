package com.pm.pulseserver.modules.posts.infra;

import com.pm.pulseserver.modules.posts.domain.PostMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostMentionRepository extends JpaRepository<PostMention, UUID> {

    List<PostMention> findByPostId(UUID postId);
}
