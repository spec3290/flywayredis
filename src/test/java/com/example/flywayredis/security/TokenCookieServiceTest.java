package com.example.flywayredis.security;

import com.example.flywayredis.dto.auth.TokenResponse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCookieServiceTest {

    @Test
    void writesAccessAndRefreshTokensAsHttpOnlyCookies() {
        TokenCookieService service = new TokenCookieService(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        TokenResponse tokenResponse = new TokenResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                1800,
                1_209_600,
                null
        );

        service.write(response, tokenResponse);

        List<String> cookies = response.getHeaders("Set-Cookie");
        assertThat(cookies).anySatisfy(cookie -> assertThat(cookie)
                .contains("ACCESS_TOKEN=access-token", "Path=/", "Secure", "HttpOnly", "SameSite=Lax"));
        assertThat(cookies).anySatisfy(cookie -> assertThat(cookie)
                .contains("REFRESH_TOKEN=refresh-token", "Path=/auth", "Secure", "HttpOnly", "SameSite=Lax"));
    }

    @Test
    void readsRefreshTokenFromCookie() {
        TokenCookieService service = new TokenCookieService(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "refresh-token"));

        assertThat(service.refreshToken(request)).contains("refresh-token");
    }
}
