package com.example.flywayredis.domain.auth;

import com.example.flywayredis.domain.user.UserResponseDto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UserResponseDto user
) {
}
