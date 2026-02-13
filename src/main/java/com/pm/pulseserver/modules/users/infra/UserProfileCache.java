package com.pm.pulseserver.modules.users.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserProfileCache {

    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private String key(String username) {
        return "user:profile:" + username.toLowerCase();
    }

    public Optional<PublicUserProfileResponse> get(String username) {
        String json = redis.opsForValue().get(key(username));

        if (json == null) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(json, PublicUserProfileResponse.class));
        } catch (Exception e) {
            redis.delete(key(username));
            return Optional.empty();
        }
    }

    public void put(String username, PublicUserProfileResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redis.opsForValue().set(key(username), json, TTL);
        } catch (JsonProcessingException ignored) {}
    }

    public void evict(String username) {
        redis.delete(key(username));
    }
}
