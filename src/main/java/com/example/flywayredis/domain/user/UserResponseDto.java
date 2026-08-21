package com.example.flywayredis.domain.user;

import java.time.LocalDateTime;

public record UserResponseDto(Long id, String nickname, String email, LocalDateTime createdAt) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
