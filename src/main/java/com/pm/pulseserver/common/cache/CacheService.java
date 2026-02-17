package com.pm.pulseserver.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);

    public<T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return type.cast(value);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_TTL);
    }

    public void delete (String key) {
        redisTemplate.delete(key);
    }

    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void decrement(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public Long getLong(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.valueOf(value.toString());
    }

    public void setLong(String key, long value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_TTL);
    }
}
