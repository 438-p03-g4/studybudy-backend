package com.example.studybudybackend.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.StudyGroupMessage;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupMessageRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ResponseStatusException;

class StudyGroupForumControllerTests {

    @Test
    void returnsGroupMembersAndMessagesForMember() {
        StudyGroupRepository studyGroupRepository = mock(StudyGroupRepository.class);
        MembershipRepository membershipRepository = mock(MembershipRepository.class);
        StudyGroupMessageRepository messageRepository = mock(StudyGroupMessageRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        User user = new User("student@example.com", "Student");
        user.setId(7L);
        StudyGroup group = new StudyGroup("CST438 Study Group", "CST438", null, "Weekly review");
        group.setId(4L);
        Membership membership = new Membership(user, group);
        StudyGroupMessage message = mock(StudyGroupMessage.class);

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("student@example.com");
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(studyGroupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByUser_IdAndStudyGroup_Id(7L, 4L)).thenReturn(true);
        when(membershipRepository.findAllByStudyGroup_IdOrderByJoinedAtAsc(4L)).thenReturn(List.of(membership));
        when(messageRepository.findAllByStudyGroup_IdOrderByCreatedAtAsc(4L)).thenReturn(List.of(message));
        when(message.getId()).thenReturn(12L);
        when(message.getContent()).thenReturn("Review the API examples?");
        when(message.getUser()).thenReturn(user);
        when(message.getCreatedAt()).thenReturn(java.time.Instant.parse("2026-11-27T18:00:00Z"));

        var response = new StudyGroupForumController(
                studyGroupRepository, membershipRepository, messageRepository, userRepository
        ).forum(4L, principal);

        assertEquals("CST438 Study Group", response.group().name());
        assertEquals("Weekly review", response.group().description());
        assertEquals(1, response.members().size());
        assertEquals("Student", response.members().getFirst().displayName());
        assertEquals("Review the API examples?", response.messages().getFirst().content());
    }

    @Test
    void rejectsForumAccessForNonMember() {
        StudyGroupRepository studyGroupRepository = mock(StudyGroupRepository.class);
        MembershipRepository membershipRepository = mock(MembershipRepository.class);
        StudyGroupMessageRepository messageRepository = mock(StudyGroupMessageRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User("student@example.com", "Student");
        user.setId(7L);
        StudyGroup group = new StudyGroup("CST438 Study Group", "CST438");
        group.setId(4L);
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("student@example.com");
        when(userRepository.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(studyGroupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByUser_IdAndStudyGroup_Id(7L, 4L)).thenReturn(false);

        StudyGroupForumController controller = new StudyGroupForumController(
                studyGroupRepository, membershipRepository, messageRepository, userRepository
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.forum(4L, principal)
        );
        assertEquals(403, exception.getStatusCode().value());
    }
}
