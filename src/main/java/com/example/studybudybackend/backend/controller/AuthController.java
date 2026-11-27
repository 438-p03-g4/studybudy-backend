package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
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

    @PatchMapping("/me/display-name")
    public UserProfile updateDisplayName(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody UpdateDisplayNameRequest request
    ) {
        User user = userRepository
                .findByEmailIgnoreCase(principal.getAttribute("email"))
                .orElseThrow();

        user.setDisplayName(request.displayName().trim());
        User savedUser = userRepository.save(user);
        return new UserProfile(savedUser.getId(), savedUser.getEmail(), savedUser.getDisplayName());
    }

    public record UserProfile(Long id, String email, String displayName) {}

    public record UpdateDisplayNameRequest(
            @NotBlank @Size(max = 120) String displayName
    ) {}
}
