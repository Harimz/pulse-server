package com.pm.pulseserver.modules.follows.app;

import com.pm.pulseserver.common.exception.BadRequestException;
import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.modules.events.app.OutboxService;
import com.pm.pulseserver.modules.events.domain.EventTypes;
import com.pm.pulseserver.modules.events.domain.OutboxStatus;
import com.pm.pulseserver.modules.follows.domain.Follow;
import com.pm.pulseserver.modules.follows.infra.FollowRepository;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional
    public void follow(UUID me, String targetUsername) {
        var target = userRepository.findByUsername(targetUsername).orElseThrow(() -> new NotFoundException("User not found"));

        if (me.equals(target.getId())) {
            throw new BadRequestException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(me, target.getId())) {
            return;
        }

        try {
            followRepository.save(Follow.builder()
                    .id(UUID.randomUUID())
                    .followerId(me)
                    .followingId(target.getId())
                    .build()
            );

            outboxService.enqueue(
                    EventTypes.USER_FOLLOWED,
                    "FOLLOW",
                    target.getId(),
                    Map.of(
                            "fromUserId", me.toString(),
                            "toUserId", target.getId().toString()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            return;
        }
    }

    @Transactional
    public void unfollow(UUID me, String targetUsername) {
        var target = userRepository.findByUsername(targetUsername).orElseThrow(() -> new NotFoundException("User not found"));

        if (me.equals(target.getId())) {
            throw new BadRequestException("You cannot unfollow yourself");
        }

        followRepository.deleteByPair(me, target.getId());
    }

    @Transactional
    public FollowCounts getCounts(String username) {
        var user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));

        long following = followRepository.countByFollowerId(user.getId());
        long followers = followRepository.countByFollowingId(user.getId());
        return new FollowCounts(followers, following);
    }

    public record FollowCounts(long followers, long following) {}
}
