package com.example.studybudybackend.backend.security;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GitHubOAuth2UserService
        implements OAuth2UserService<
        @NonNull OAuth2UserRequest,
        @NonNull OAuth2User
        > {

    private static final ParameterizedTypeReference<
            List<Map<String, Object>>
            > EMAIL_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

    private final DefaultOAuth2UserService githubUserService =
            new DefaultOAuth2UserService();

    private final RestClient restClient = RestClient.create();
    private final UserRepository userRepository;

    public GitHubOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {
        OAuth2User githubUser = githubUserService.loadUser(request);
        String email = getPrimaryVerifiedEmail(request);

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_unavailable"),
                    "GitHub did not provide a primary verified email. "
                            + "Grant user:email access and try again."
            );
        }

        String name = (String) githubUser.getAttributes().get("name");
        String login = (String) githubUser.getAttributes().get("login");

        final String displayName =
                name != null && !name.isBlank() ? name
                        : login != null && !login.isBlank() ? login
                        : email;

        userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(new User(email, displayName)));

        // Include the verified email for the shared /api/auth/me endpoint.
        Map<String, Object> attributes =
                new HashMap<>(githubUser.getAttributes());
        attributes.put("email", email);

        return new DefaultOAuth2User(
                githubUser.getAuthorities(),
                attributes,
                "id"
        );
    }

    private String getPrimaryVerifiedEmail(OAuth2UserRequest request) {
        List<Map<String, Object>> emails;

        try {
            RestClient.RequestHeadersSpec<?> emailRequest = restClient.get()
                    .uri("https://api.github.com/user/emails");

            emailRequest.headers(headers -> {
                headers.setBearerAuth(request.getAccessToken().getTokenValue());
                headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
                headers.set("X-GitHub-Api-Version", "2026-03-10");
            });
            emails = emailRequest.retrieve().body(EMAIL_RESPONSE_TYPE);
        } catch (RestClientException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_lookup_failed"),
                    "Could not retrieve the verified email from GitHub.",
                    exception
            );
        }

        if (emails == null) {
            return null;
        }

        return emails.stream()
                .filter(email ->
                        Boolean.TRUE.equals(email.get("primary"))
                                && Boolean.TRUE.equals(email.get("verified"))
                )
                .map(email -> email.get("email"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElse(null);
    }
}
