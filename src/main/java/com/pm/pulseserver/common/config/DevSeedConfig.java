package com.pm.pulseserver.common.config;

import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.domain.UserProfile;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSeedConfig implements CommandLineRunner {

    private final UserRepository users;

    @Override
    public void run(String... args) {
        if (users.existsByUsername("harim")) return;

        UUID id = UUID.randomUUID();

        User user = User.builder()
                .id(id)
                .username("harim")
                .email("harim@example.com")
                .passwordHash("dev-not-real")
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .userId(id)
                .displayName("Harim Zermeno")
                .bio("Building Pulse — a portfolio-grade activity feed app.")
                .avatarUrl(null)
                .build();

        user.setProfile(profile);
        users.save(user);
    }
}
