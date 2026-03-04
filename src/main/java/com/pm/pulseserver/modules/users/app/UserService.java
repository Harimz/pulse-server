package com.pm.pulseserver.modules.users.app;

import com.pm.pulseserver.common.cache.CacheService;
import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.modules.follows.app.FollowService;
import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import com.pm.pulseserver.modules.users.api.dto.UpdateMyProfileRequest;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.domain.UserProfile;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final FollowService followService;

    public PublicUserProfileResponse getPublicUserProfile(String username, UUID loggedInUser) {

        String key = "user:profile:" + username + ":viewer:" + loggedInUser;

        var cached = cacheService.get(key, PublicUserProfileResponse.class);
        if (cached != null) {
            return cached;
        }

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile profile = user.getProfile();

        var followCounts = followService.getCounts(user.getUsername());

        boolean isFollowing = followService.isFollowing(loggedInUser, user.getId());

        PublicUserProfileResponse dto = new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getBio() : null,
                profile != null ? profile.getAvatarUrl() : null,
                isFollowing,
                followCounts.followers(),
                followCounts.following()
        );

        cacheService.set(key, dto);

        return dto;
    }

    public PublicUserProfileResponse getMyPublicUserProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile profile = user.getProfile();

        var followCounts = followService.getCounts(user.getUsername());

        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                false,
                followCounts.followers(),
                followCounts.following()
        );
    }

    @Transactional
    public PublicUserProfileResponse updateMyProfile(UUID userId, String username, UpdateMyProfileRequest req) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .userId(user.getId())
                    .bio(null)
                    .avatarUrl(null)
                    .build();
            user.setProfile(profile);
        }

        if (req.displayName() != null) {
            profile.setDisplayName(req.displayName().trim());
        }

        if (req.bio() != null) {
            String bio = req.bio().trim();
            profile.setBio(bio.isEmpty() ? null : bio);
        }

        if (req.avatarUrl() != null) {
            String avatarUrl = req.avatarUrl().trim();
            profile.setAvatarUrl(avatarUrl.isEmpty() ? null : avatarUrl);
        }

        userRepository.save(user);

        cacheService.delete("user:profile:" + username);
        var followCounts = followService.getCounts(username);


        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                false,
                followCounts.followers(),
                followCounts.following()
        );
    }
}
