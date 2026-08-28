package com.example.flywayredis.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(String tokenId, Long userId, Duration expiration) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + tokenId,
                userId.toString(),
                expiration
        );
    }

    public Long consume(String tokenId) {
        String userId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + tokenId);
        return userId != null ? Long.valueOf(userId) : null;
    }
}
