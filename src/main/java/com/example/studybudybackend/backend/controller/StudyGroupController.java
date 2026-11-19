package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupRepository studyGroupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public StudyGroupController(
            StudyGroupRepository studyGroupRepository,
            MembershipRepository membershipRepository,
            UserRepository userRepository
    ) {
        this.studyGroupRepository = studyGroupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<StudyGroupSummary>> searchGroups(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String course
    ) {
        List<StudyGroup> groups;
        String trimmedQuery = query == null ? "" : query.trim();

        if (!trimmedQuery.isEmpty()) {
            groups = studyGroupRepository.searchByQuery(trimmedQuery);
        } else if (course != null && !course.trim().isEmpty()) {
            groups = studyGroupRepository.searchByCourse(course);
        } else {
            groups = new ArrayList<>(studyGroupRepository.findAll());
            Collections.shuffle(groups);
        }

        List<StudyGroupSummary> results = groups.stream()
                .limit(20)
                .map(this::toSummary)
                .toList();

        return ResponseEntity.ok(results);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<StudyGroupSummary> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        String email = principal == null
                ? null
                : principal.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "The authenticated user email is missing."
            );
        }

        User creator = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "The authenticated user could not be found."
                ));

        StudyGroup group = new StudyGroup(
                request.name().trim(),
                request.course().trim(),
                normalizeOptional(request.iconUrl()),
                normalizeOptional(request.description())
        );

        StudyGroup savedGroup = studyGroupRepository.save(group);

        Membership ownerMembership = new Membership(creator, savedGroup);
        ownerMembership.setRole("OWNER");
        membershipRepository.save(ownerMembership);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toSummary(savedGroup));
    }

    private StudyGroupSummary toSummary(StudyGroup group) {
        return new StudyGroupSummary(
                group.getId(),
                group.getName(),
                group.getCourse(),
                group.getIconUrl(),
                group.getDescription()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public record CreateGroupRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 40) String course,
            @Size(max = 500) String iconUrl,
            @Size(max = 1000) String description
    ) {
    }

    public record StudyGroupSummary(
            Long id,
            String name,
            String course,
            String iconUrl,
            String description
    ) {
    }
}
