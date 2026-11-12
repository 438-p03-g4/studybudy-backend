package com.example.studybudybackend.backend.security;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class GoogleOidcUserService
        implements OAuth2UserService<
        @NonNull OidcUserRequest,
        @NonNull OidcUser
        > {
    private final OAuth2UserService<OidcUserRequest, OidcUser> googleUserService;
    private final UserRepository userRepository;

    @Autowired
    public GoogleOidcUserService(UserRepository userRepository) {
        this(userRepository, new OidcUserService());
    }

    GoogleOidcUserService(
            UserRepository userRepository,
            OAuth2UserService<OidcUserRequest, OidcUser> googleUserService
    ) {
        this.userRepository = userRepository;
        this.googleUserService = googleUserService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request)
            throws OAuth2AuthenticationException {
        OidcUser googleUser = googleUserService.loadUser(request);

        String email = googleUser.getEmail();
        if (email == null || !Boolean.TRUE.equals(googleUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email"),
                    "Google did not provide a verified email address."
            );
        }

        String fullName = googleUser.getFullName();
        final String displayName =
                (fullName == null || fullName.isBlank()) ? email : fullName;

        userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(new User(email, displayName)));

        return googleUser;
    }
}
