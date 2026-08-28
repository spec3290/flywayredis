package com.example.flywayredis.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(@NotBlank String nickname, @NotBlank String email, @NotBlank String password) {
}
