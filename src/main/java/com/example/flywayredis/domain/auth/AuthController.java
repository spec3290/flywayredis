package com.example.flywayredis.domain.auth;

import com.example.flywayredis.domain.user.UserResponseDto;
import com.example.flywayredis.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieService tokenCookieService;

    @GetMapping("/oauth2/login")
    public RedirectView githubLogin() {
        return new RedirectView("/oauth2/authorization/github");
    }

    @PostMapping("/login")
    public ApiResponse<UserResponseDto> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.login(request);
        tokenCookieService.write(response, tokenResponse);
        return ApiResponse.success(tokenResponse.user());
    }

    @PostMapping("/refresh")
    public ApiResponse<UserResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = tokenCookieService.refreshToken(request)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰 쿠키가 없습니다."));
        TokenResponse tokenResponse = authService.refresh(new RefreshTokenRequest(refreshToken));
        tokenCookieService.write(response, tokenResponse);
        return ApiResponse.success(tokenResponse.user());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        tokenCookieService.refreshToken(request).ifPresent(authService::logout);
        tokenCookieService.clear(response);
        return ApiResponse.success(null);
    }

    @PostMapping("/join")
    public ApiResponse<UserResponseDto> join(@Valid @RequestBody JoinRequest request) {
        return ApiResponse.success(authService.join(request));
    }

    @GetMapping({"/me", "/token/me"})
    public ApiResponse<UserResponseDto> tokenMe(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(authService.getUser(Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/csrf")
    public ApiResponse<Void> csrf(CsrfToken csrfToken) {
        return ApiResponse.success(null);
    }
}
