package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.StudyGroupMessage;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupMessageRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/groups")
public class StudyGroupForumController {
    private final StudyGroupRepository studyGroupRepository;
    private final MembershipRepository membershipRepository;
    private final StudyGroupMessageRepository messageRepository;
    private final UserRepository userRepository;

    public StudyGroupForumController(
            StudyGroupRepository studyGroupRepository,
            MembershipRepository membershipRepository,
            StudyGroupMessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.studyGroupRepository = studyGroupRepository;
        this.membershipRepository = membershipRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/mine")
    public List<StudyGroupController.StudyGroupSummary> myGroups(
            @AuthenticationPrincipal OAuth2User principal
    ) {
        User user = authenticatedUser(principal);
        return membershipRepository.findAllByUser_IdOrderByJoinedAtDesc(user.getId()).stream()
                .map(Membership::getStudyGroup)
                .map(group -> new StudyGroupController.StudyGroupSummary(
                        group.getId(), group.getName(), group.getCourse(), group.getIconUrl(), group.getDescription()
                ))
                .toList();
    }

    @GetMapping("/{groupId}/forum")
    public ForumResponse forum(
            @PathVariable Long groupId,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        User user = authenticatedUser(principal);
        StudyGroup group = findGroup(groupId);
        requireMember(user, groupId);

        List<ForumMember> members = membershipRepository.findAllByStudyGroup_IdOrderByJoinedAtAsc(groupId).stream()
                .map(membership -> new ForumMember(
                        membership.getUser().getId(),
                        membership.getUser().getDisplayName()
                ))
                .toList();
        List<ForumMessage> messages = messageRepository.findAllByStudyGroup_IdOrderByCreatedAtAsc(groupId).stream()
                .map(message -> new ForumMessage(
                        message.getId(),
                        message.getContent(),
                        message.getUser().getId(),
                        message.getUser().getDisplayName(),
                        message.getCreatedAt()
                ))
                .toList();

        return new ForumResponse(
                new ForumGroup(group.getId(), group.getName(), group.getCourse(), group.getDescription()),
                members,
                messages
        );
    }

    @PostMapping("/{groupId}/messages")
    public ForumMessage createMessage(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        User user = authenticatedUser(principal);
        StudyGroup group = findGroup(groupId);
        requireMember(user, groupId);
        StudyGroupMessage saved = messageRepository.save(
                new StudyGroupMessage(group, user, request.content().trim())
        );
        return new ForumMessage(
                saved.getId(), saved.getContent(), user.getId(), user.getDisplayName(), saved.getCreatedAt()
        );
    }

    private User authenticatedUser(OAuth2User principal) {
        String email = principal == null ? null : principal.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The authenticated user email is missing.");
        }
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The authenticated user could not be found."));
    }

    private StudyGroup findGroup(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study group was not found."));
    }

    private void requireMember(User user, Long groupId) {
        if (!membershipRepository.existsByUser_IdAndStudyGroup_Id(user.getId(), groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of this study group.");
        }
    }

    public record CreateMessageRequest(@NotBlank @Size(max = 4000) String content) {}
    public record ForumGroup(Long id, String name, String course, String description) {}
    public record ForumMember(Long id, String displayName) {}
    public record ForumMessage(Long id, String content, Long authorId, String authorName, Instant createdAt) {}
    public record ForumResponse(ForumGroup group, List<ForumMember> members, List<ForumMessage> messages) {}
}
