package com.example.flywayredis.service;

import com.example.flywayredis.dto.auth.TokenResponse;
import com.example.flywayredis.repository.RefreshTokenStore;

import com.example.flywayredis.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtTokenServiceTest {

    @Test
    void issueCreatesSignedAccessTokenWithUserClaims() {
        SecretKey key = new SecretKeySpec(
                "test-jwt-secret-key-that-is-at-least-32-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        NimbusJwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key)
                .algorithm(MacAlgorithm.HS256)
                .build();
        NimbusJwtDecoder accessTokenDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        NimbusJwtDecoder refreshTokenDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        JwtTokenService tokenService = new JwtTokenService(
                encoder,
                refreshTokenDecoder,
                refreshTokenStore,
                "flywayredis",
                Duration.ofMinutes(30),
                Duration.ofDays(14)
        );

        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        when(user.getNickname()).thenReturn("buyer");
        when(user.getEmail()).thenReturn("buyer@example.com");

        TokenResponse response = tokenService.issue(user);
        Jwt accessToken = accessTokenDecoder.decode(response.accessToken());
        Jwt refreshToken = refreshTokenDecoder.decode(response.refreshToken());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(1800);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(1_209_600);
        assertThat(accessToken.getSubject()).isEqualTo("2");
        assertThat(accessToken.getClaimAsString("token_type")).isEqualTo("ACCESS");
        assertThat(accessToken.getClaimAsString("email")).isEqualTo("buyer@example.com");
        assertThat(accessToken.getClaimAsString("nickname")).isEqualTo("buyer");
        assertThat(refreshToken.getSubject()).isEqualTo("2");
        assertThat(refreshToken.getClaimAsString("token_type")).isEqualTo("REFRESH");
        verify(refreshTokenStore).save(refreshToken.getId(), 2L, Duration.ofDays(14));

        when(refreshTokenStore.consume(refreshToken.getId())).thenReturn(2L);
        assertThat(tokenService.consumeRefreshToken(response.refreshToken())).isEqualTo(2L);
    }
}
