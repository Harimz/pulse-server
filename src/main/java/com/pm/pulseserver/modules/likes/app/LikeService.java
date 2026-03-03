package com.pm.pulseserver.modules.likes.app;

import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.modules.likes.domain.PostLike;
import com.pm.pulseserver.modules.likes.infra.PostLikeRepository;
import com.pm.pulseserver.modules.posts.infra.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;

    @Transactional
    public void like(UUID me, UUID postId) {
        if (!postRepository.existsById(postId)) throw new NotFoundException("Post not found");

        try {
            likeRepository.save(PostLike.builder()
                    .id(UUID.randomUUID())
                    .postId(postId)
                    .userId(me)
                    .build());
        } catch (DataIntegrityViolationException ignored) {
        }
    }


    @Transactional
    public void unlike(UUID me, UUID postId) {
        if (!postRepository.existsById(postId)) throw new NotFoundException("Post not found");
        likeRepository.deleteByPostIdAndUserId(postId, me);
    }
}
