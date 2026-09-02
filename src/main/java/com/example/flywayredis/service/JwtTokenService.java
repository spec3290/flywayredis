package com.example.flywayredis.service;

import com.example.flywayredis.dto.auth.TokenResponse;
import com.example.flywayredis.repository.RefreshTokenStore;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.dto.user.UserResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenJwtDecoder;
    private final RefreshTokenStore refreshTokenStore;
    private final String issuer;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Qualifier("refreshTokenJwtDecoder") JwtDecoder refreshTokenJwtDecoder,
            RefreshTokenStore refreshTokenStore,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-token-expiration}") Duration accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") Duration refreshTokenExpiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenJwtDecoder = refreshTokenJwtDecoder;
        this.refreshTokenStore = refreshTokenStore;
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public TokenResponse issue(User user) {
        Instant issuedAt = Instant.now();
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        JwtClaimsSet accessTokenClaims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(accessTokenExpiration))
                .id(accessTokenId)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .claim("scope", "USER")
                .claim("token_type", "ACCESS")
                .build();

        JwtClaimsSet refreshTokenClaims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(refreshTokenExpiration))
                .id(refreshTokenId)
                .subject(user.getId().toString())
                .claim("token_type", "REFRESH")
                .build();

        String accessToken = encode(accessTokenClaims);
        String refreshToken = encode(refreshTokenClaims);
        refreshTokenStore.save(refreshTokenId, refreshToken, refreshTokenExpiration);

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration.toSeconds(),
                refreshTokenExpiration.toSeconds(),
                UserResponseDto.from(user)
        );
    }

    public Long consumeRefreshToken(String refreshToken) {
        try {
            Jwt jwt = refreshTokenJwtDecoder.decode(refreshToken);
            Long subjectUserId = Long.valueOf(jwt.getSubject());
            String storedRefreshToken = refreshTokenStore.consume(jwt.getId());

            if (!matches(storedRefreshToken, refreshToken)) {
                throw new IllegalArgumentException("이미 사용되었거나 만료된 리프레시 토큰입니다.");
            }
            return subjectUserId;
        } catch (JwtException | NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    public void revokeRefreshToken(String refreshToken) {
        try {
            Jwt jwt = refreshTokenJwtDecoder.decode(refreshToken);
            refreshTokenStore.consume(jwt.getId());
        } catch (JwtException ignored) {
            // 로그아웃은 쿠키 삭제가 목적이므로 이미 만료되거나 잘못된 토큰도 성공 처리합니다.
        }
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private boolean matches(String storedToken, String presentedToken) {
        return storedToken != null && MessageDigest.isEqual(
                storedToken.getBytes(StandardCharsets.UTF_8),
                presentedToken.getBytes(StandardCharsets.UTF_8)
        );
    }
}