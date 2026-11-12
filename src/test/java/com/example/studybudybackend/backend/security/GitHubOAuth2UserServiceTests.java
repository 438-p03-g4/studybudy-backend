package com.example.studybudybackend.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubOAuth2UserServiceTests {

    private static final String EMAILS_URL = "https://api.github.com/user/emails";

    private UserRepository userRepository;
    private MockRestServiceServer server;
    private GitHubOAuth2UserService service;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        OAuth2User githubUser = new DefaultOAuth2User(
                List.of(),
                Map.of("id", 12345, "login", "study-user", "name", "Study User"),
                "id"
        );
        service = new GitHubOAuth2UserService(
                userRepository,
                request -> githubUser,
                restClient
        );
    }

    @Test
    void savesUserFromPrimaryVerifiedGitHubEmailAndReturnsEmailAttribute() {
        server.expect(requestTo(EMAILS_URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-access-token"))
                .andExpect(header(HttpHeaders.ACCEPT, "application/vnd.github+json"))
                .andRespond(withSuccess("""
                        [
                          {"email":"other@example.com","primary":false,"verified":true},
                          {"email":"study@example.com","primary":true,"verified":true}
                        ]
                        """, MediaType.APPLICATION_JSON));
        when(userRepository.findByEmailIgnoreCase("study@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User principal = service.loadUser(githubRequest());

        assertEquals("study@example.com", principal.getAttribute("email"));
        assertEquals("12345", principal.getName());
        ArgumentCaptor<User> savedUser = forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("study@example.com", savedUser.getValue().getEmail());
        assertEquals("Study User", savedUser.getValue().getDisplayName());
        server.verify();
    }

    @Test
    void rejectsGitHubAccountWithoutPrimaryVerifiedEmail() {
        server.expect(requestTo(EMAILS_URL))
                .andRespond(withSuccess("""
                        [
                          {"email":"unverified@example.com","primary":true,"verified":false}
                        ]
                        """, MediaType.APPLICATION_JSON));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(githubRequest())
        );

        assertEquals("github_email_unavailable", exception.getError().getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        server.verify();
    }

    @Test
    void rejectsWhenGitHubEmailEndpointFails() {
        server.expect(requestTo(EMAILS_URL))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withStatus(HttpStatus.FORBIDDEN));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(githubRequest())
        );

        assertEquals("github_email_lookup_failed", exception.getError().getErrorCode());
        verify(userRepository, never()).save(any(User.class));
        server.verify();
    }

    private OAuth2UserRequest githubRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("github")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/github")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-access-token",
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                Set.of("read:user", "user:email")
        );

        return new OAuth2UserRequest(registration, accessToken);
    }
}
