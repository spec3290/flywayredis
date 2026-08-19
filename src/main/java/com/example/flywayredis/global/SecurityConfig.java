package com.example.flywayredis.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/auth/login",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/chat-rooms/**",
                                "/ws-chat"
                        ).permitAll()
                        .requestMatchers("/auth/me").authenticated()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/chat-rooms/**",
                        "/products/**",
                        "/ws-chat/**"
                ))
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/auth/me", true)
                )
                .build();
    }
}
