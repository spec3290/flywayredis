package com.example.flywayredis.dto.auth;

import com.example.flywayredis.dto.user.UserResponseDto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UserResponseDto user
) {
}
