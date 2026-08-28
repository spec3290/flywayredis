package com.example.flywayredis.config;

import com.example.flywayredis.security.CookieBearerTokenResolver;

import com.example.flywayredis.security.OAuth2LoginSuccessHandler;
import com.example.flywayredis.common.response.ApiResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    private static final String JWT_BEARER_AUTH = "bearerAuth";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            CookieBearerTokenResolver cookieBearerTokenResolver
    ) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/auth/join",
                                "/auth/csrf",
                                "/auth/oauth2/login",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.spa())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                                response,
                                objectMapper,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "UNAUTHORIZED",
                                "인증이 필요합니다."
                        ))
                        .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                                response,
                                objectMapper,
                                HttpServletResponse.SC_FORBIDDEN,
                                "FORBIDDEN",
                                "접근 권한이 없습니다."
                        ))
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(cookieBearerTokenResolver)
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    @Bean
    public OpenAPI flywayRedisOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 API 로 발급받은 JWT 를 입력하세요.");

        return new OpenAPI()
                .info(new Info()
                        .title("플라이웨이레디스 API")
                        .description("중고거래 서비스 - Spring Boot 실습 프로젝트")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(JWT_BEARER_AUTH, bearerScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList(JWT_BEARER_AUTH));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeSecurityError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String code,
            String message
    ) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(code, message));
    }
}
