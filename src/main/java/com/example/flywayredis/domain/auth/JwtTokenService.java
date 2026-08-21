package com.example.flywayredis.domain.auth;

import com.example.flywayredis.domain.user.User;
import com.example.flywayredis.domain.user.UserResponseDto;
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
        refreshTokenStore.save(refreshTokenId, user.getId(), refreshTokenExpiration);

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
            Long storedUserId = refreshTokenStore.consume(jwt.getId());

            if (storedUserId == null || !storedUserId.equals(subjectUserId)) {
                throw new IllegalArgumentException("이미 사용되었거나 만료된 리프레시 토큰입니다.");
            }
            return subjectUserId;
        } catch (JwtException | NumberFormatException exception) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
