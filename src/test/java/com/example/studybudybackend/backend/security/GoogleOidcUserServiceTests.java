package com.example.studybudybackend.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleOidcUserServiceTests {

    private UserRepository userRepository;
    private OidcUser googleUser;
    private GoogleOidcUserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        googleUser = mock(OidcUser.class);
        service = new GoogleOidcUserService(userRepository, request -> googleUser);
    }

    @Test
    void savesNewUserWhenGoogleEmailIsVerified() {
        when(googleUser.getEmail()).thenReturn("student@example.com");
        when(googleUser.getEmailVerified()).thenReturn(true);
        when(googleUser.getFullName()).thenReturn("Student Name");
        when(userRepository.findByEmailIgnoreCase("student@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OidcUser result = service.loadUser(mock(OidcUserRequest.class));

        assertEquals(googleUser, result);
        ArgumentCaptor<User> savedUser = forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("student@example.com", savedUser.getValue().getEmail());
        assertEquals("Student Name", savedUser.getValue().getDisplayName());
    }

    @Test
    void rejectsUnverifiedGoogleEmail() {
        when(googleUser.getEmail()).thenReturn("student@example.com");
        when(googleUser.getEmailVerified()).thenReturn(false);

        assertThrows(
                OAuth2AuthenticationException.class,
                () -> service.loadUser(mock(OidcUserRequest.class))
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
