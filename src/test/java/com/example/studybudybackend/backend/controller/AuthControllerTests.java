package com.example.studybudybackend.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AuthControllerTests {

    @Test
    void returnsCurrentUserProfileForOAuthPrincipalEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User("student@example.com", "Student Name");
        user.setId(42L);
        when(userRepository.findByEmailIgnoreCase("student@example.com"))
                .thenReturn(Optional.of(user));

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("student@example.com");

        AuthController.UserProfile profile =
                new AuthController(userRepository).currentUser(principal);

        assertEquals(42L, profile.id());
        assertEquals("student@example.com", profile.email());
        assertEquals("Student Name", profile.displayName());
        verify(userRepository).findByEmailIgnoreCase("student@example.com");
    }
}
