package com.example.flywayredis.service;

import com.example.flywayredis.dto.auth.TokenResponse;
import com.example.flywayredis.entity.OAuthAccount;
import com.example.flywayredis.entity.OAuthProvider;
import com.example.flywayredis.repository.OAuthAccountRepository;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OAuth2AuthService {

    private static final OAuthProvider PROVIDER = OAuthProvider.GITHUB;

    private final OAuthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public TokenResponse login(OAuth2User oauth2User) {
        String providerUserId = githubUserId(oauth2User);

        return oauthAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(OAuthAccount::getUser)
                .map(jwtTokenService::issue)
                .orElseGet(() -> registerAndIssue(oauth2User, providerUserId));
    }

    private TokenResponse registerAndIssue(OAuth2User oauth2User, String providerUserId) {
        String nickname = uniqueNickname(oauth2User.getAttribute("login"), providerUserId);
        String email = uniqueEmail(oauth2User.getAttribute("email"), providerUserId);
        String disabledPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = userRepository.save(User.create(nickname, email, disabledPassword));
        oauthAccountRepository.save(OAuthAccount.create(user, PROVIDER, providerUserId));
        return jwtTokenService.issue(user);
    }

    private String githubUserId(OAuth2User oauth2User) {
        Number githubId = oauth2User.getAttribute("id");
        if (githubId == null) {
            throw new IllegalArgumentException("GitHub 사용자 ID를 확인할 수 없습니다.");
        }
        return githubId.toString();
    }

    private String uniqueNickname(String githubLogin, String providerUserId) {
        String baseNickname = githubLogin == null || githubLogin.isBlank()
                ? "github-user"
                : githubLogin.trim();

        if (!userRepository.existsByNickname(baseNickname)) {
            return baseNickname;
        }
        return baseNickname + "-" + providerUserId;
    }

    private String uniqueEmail(String githubEmail, String providerUserId) {
        if (githubEmail != null
                && !githubEmail.isBlank()
                && !userRepository.existsByEmail(githubEmail.trim())) {
            return githubEmail.trim();
        }
        return "github-" + providerUserId + "@oauth.local";
    }
}
