package com.example.flywayredis.security;

import com.example.flywayredis.dto.auth.TokenResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class TokenCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    private final boolean secure;

    public TokenCookieService(@Value("${app.auth.cookie-secure:true}") boolean secure) {
        this.secure = secure;
    }

    public void write(HttpServletResponse response, TokenResponse tokenResponse) {
        addCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                tokenResponse.accessToken(),
                "/",
                Duration.ofSeconds(tokenResponse.accessTokenExpiresIn())
        );
        addCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                tokenResponse.refreshToken(),
                "/auth",
                Duration.ofSeconds(tokenResponse.refreshTokenExpiresIn())
        );
    }

    public Optional<String> accessToken(HttpServletRequest request) {
        return cookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> refreshToken(HttpServletRequest request) {
        return cookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    public void clear(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", "/", Duration.ZERO);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", "/auth", Duration.ZERO);
    }

    private Optional<String> cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            String path,
            Duration maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
