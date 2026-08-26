package com.example.flywayredis.domain.auth;

import com.example.flywayredis.domain.user.User;
import com.example.flywayredis.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2AuthServiceTest {

    @Test
    void existingGithubAccountIssuesServiceTokenForLinkedUser() {
        OAuthAccountRepository accountRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        OAuth2AuthService service = new OAuth2AuthService(
                accountRepository,
                userRepository,
                passwordEncoder,
                jwtTokenService
        );
        OAuth2User oauth2User = mock(OAuth2User.class);
        OAuthAccount account = mock(OAuthAccount.class);
        User user = mock(User.class);
        TokenResponse expected = mock(TokenResponse.class);

        when(oauth2User.getAttribute("id")).thenReturn(1234);
        when(accountRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, "1234"))
                .thenReturn(Optional.of(account));
        when(account.getUser()).thenReturn(user);
        when(jwtTokenService.issue(user)).thenReturn(expected);

        assertThat(service.login(oauth2User)).isSameAs(expected);
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenService).issue(user);
    }

    @Test
    void firstGithubLoginCreatesInternalUserAndAccount() {
        OAuthAccountRepository accountRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenService jwtTokenService = mock(JwtTokenService.class);
        OAuth2AuthService service = new OAuth2AuthService(
                accountRepository,
                userRepository,
                passwordEncoder,
                jwtTokenService
        );
        OAuth2User oauth2User = mock(OAuth2User.class);
        TokenResponse expected = mock(TokenResponse.class);

        when(oauth2User.getAttribute("id")).thenReturn(1234);
        when(oauth2User.getAttribute("login")).thenReturn("octocat");
        when(oauth2User.getAttribute("email")).thenReturn("octocat@example.com");
        when(accountRepository.findByProviderAndProviderUserId(OAuthProvider.GITHUB, "1234"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByNickname("octocat")).thenReturn(false);
        when(userRepository.existsByEmail("octocat@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("disabled-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.issue(any(User.class))).thenReturn(expected);

        assertThat(service.login(oauth2User)).isSameAs(expected);
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(OAuthAccount.class));
    }
}
