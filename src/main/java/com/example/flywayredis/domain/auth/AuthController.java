package com.example.flywayredis.domain.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView("/oauth2/authorization/github");
    }

    @GetMapping("/me")
    public GitHubUserResponse me(@AuthenticationPrincipal OAuth2User user) {
        Number githubId = user.getAttribute("id");

        return new GitHubUserResponse(
                githubId != null ? githubId.longValue() : null,
                user.getAttribute("login"),
                user.getAttribute("name"),
                user.getAttribute("avatar_url")
        );
    }

    public record GitHubUserResponse(
            Long githubId,
            String login,
            String name,
            String avatarUrl
    ) {
    }
}
