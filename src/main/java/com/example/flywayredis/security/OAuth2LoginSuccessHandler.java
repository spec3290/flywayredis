package com.example.flywayredis.security;

import com.example.flywayredis.dto.auth.TokenResponse;
import com.example.flywayredis.service.OAuth2AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthService oauth2AuthService;
    private final TokenCookieService tokenCookieService;
    private final String successRedirectUri;

    public OAuth2LoginSuccessHandler(
            OAuth2AuthService oauth2AuthService,
            TokenCookieService tokenCookieService,
            @Value("${app.oauth2.success-redirect-uri:/auth/me}") String successRedirectUri
    ) {
        this.oauth2AuthService = oauth2AuthService;
        this.tokenCookieService = tokenCookieService;
        this.successRedirectUri = successRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        TokenResponse tokenResponse = oauth2AuthService.login(oauth2User);
        tokenCookieService.write(response, tokenResponse);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        response.sendRedirect(successRedirectUri);
    }
}
