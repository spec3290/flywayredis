package com.example.flywayredis.service;

import com.example.flywayredis.dto.auth.JoinRequest;
import com.example.flywayredis.dto.auth.LoginRequest;
import com.example.flywayredis.dto.auth.TokenResponse;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void joinStoresEncodedPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder, jwtTokenService);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.join(new JoinRequest("buyer", "buyer@example.com", "password123"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                !user.getPassword().equals("password123")
                        && passwordEncoder.matches("password123", user.getPassword())
        ));
    }

    @Test
    void loginIssuesTokenWhenPasswordMatches() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder, jwtTokenService);
        User user = mock(User.class);
        TokenResponse expected = new TokenResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                1800,
                1_209_600,
                null
        );

        when(user.getPassword()).thenReturn(passwordEncoder.encode("password123"));
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenService.issue(user)).thenReturn(expected);

        TokenResponse response = authService.login(new LoginRequest("buyer@example.com", "password123"));

        assertThat(response).isSameAs(expected);
        verify(jwtTokenService).issue(user);
    }
}
