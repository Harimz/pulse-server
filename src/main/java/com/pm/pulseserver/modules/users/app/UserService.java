package com.pm.pulseserver.modules.users.app;

import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.domain.UserProfile;
import com.pm.pulseserver.modules.users.infra.UserProfileCache;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileCache cache;

    @Transactional(readOnly = true)
    public PublicUserProfileResponse getPublicUserProfile(String username) {
        return cache.get(username).orElseGet(() -> {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));

            UserProfile profile = user.getProfile();

            PublicUserProfileResponse dto = new PublicUserProfileResponse(
                    user.getId(),
                    user.getUsername(),
                    profile != null ? profile.getDisplayName() : user.getUsername(),
                    profile != null ? profile.getBio() : null,
                    profile != null ? profile.getAvatarUrl() : null
            );

            cache.put(username, dto);

            return dto;
        });
    }
}
