package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
// TODO: ADD COMMENTS
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserProfile currentUser(@AuthenticationPrincipal OAuth2User principal) {
        User user = userRepository
                .findByEmailIgnoreCase(principal.getAttribute("email"))
                .orElseThrow();

        return new UserProfile(user.getId(), user.getEmail(), user.getDisplayName());
    }

    public record UserProfile(Long id, String email, String displayName) {}
}
