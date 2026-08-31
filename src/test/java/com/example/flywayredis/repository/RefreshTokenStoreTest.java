package com.example.flywayredis.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenStoreTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final RefreshTokenStore refreshTokenStore = new RefreshTokenStore(redisTemplate);

    @Test
    void savesRefreshTokenWithExpiration() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        refreshTokenStore.save("token-id", "refresh-token", Duration.ofDays(14));

        verify(valueOperations).set(
                "auth:refresh:token-id",
                "refresh-token",
                Duration.ofDays(14)
        );
    }

    @Test
    void consumesRefreshTokenAtomically() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete("auth:refresh:token-id")).thenReturn("refresh-token");

        String refreshToken = refreshTokenStore.consume("token-id");

        assertThat(refreshToken).isEqualTo("refresh-token");
        verify(valueOperations).getAndDelete("auth:refresh:token-id");
    }
}
