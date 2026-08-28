package com.example.flywayredis.service;

import com.example.flywayredis.dto.auth.JoinRequest;
import com.example.flywayredis.dto.auth.LoginRequest;
import com.example.flywayredis.dto.auth.RefreshTokenRequest;
import com.example.flywayredis.dto.auth.TokenResponse;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import com.example.flywayredis.dto.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtTokenService.issue(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        Long userId = jwtTokenService.consumeRefreshToken(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return jwtTokenService.issue(user);
    }

    @Transactional
    public UserResponseDto join(JoinRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        User user = User.create(
                request.nickname(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        return UserResponseDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return UserResponseDto.from(user);
    }

    public void logout(String refreshToken) {
        jwtTokenService.revokeRefreshToken(refreshToken);
    }
}
