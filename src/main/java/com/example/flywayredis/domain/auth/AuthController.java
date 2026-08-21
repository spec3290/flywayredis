package com.example.flywayredis.domain.auth;

import com.example.flywayredis.domain.user.UserResponseDto;
import com.example.flywayredis.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/oauth2/login")
    public RedirectView githubLogin() {
        return new RedirectView("/oauth2/authorization/github");
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/join")
    public ApiResponse<UserResponseDto> join(@Valid @RequestBody JoinRequest request) {
        return ApiResponse.success(authService.join(request));
    }

    @GetMapping("/token/me")
    public ApiResponse<UserResponseDto> tokenMe(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(authService.getUser(Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/me")
    public ApiResponse<GitHubUserResponse> me(@AuthenticationPrincipal OAuth2User user) {
        Number githubId = user.getAttribute("id");

        return ApiResponse.success(new GitHubUserResponse(
                githubId != null ? githubId.longValue() : null,
                user.getAttribute("login"),
                user.getAttribute("name"),
                user.getAttribute("avatar_url")
        ));
    }

    public record GitHubUserResponse(
            Long githubId,
            String login,
            String name,
            String avatarUrl
    ) {
    }
}
