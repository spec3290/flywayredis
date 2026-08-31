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

    public void save(String tokenId, String refreshToken, Duration expiration) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + tokenId,
                refreshToken,
                expiration
        );
    }

    public String consume(String tokenId) {
        return redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + tokenId);
    }
}
