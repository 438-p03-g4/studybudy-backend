package com.example.studybudybackend.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.StudySession;
import com.example.studybudybackend.backend.domain.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

// Repository tests use an in-memory database, never Neon.
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:studybudy-repository;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Test
    void userRepositoryFindsEmailWithoutCaseSensitivity() {
        userRepository.save(new User("sam@example.edu", "Sam"));

        assertTrue(userRepository.existsByEmailIgnoreCase("SAM@example.edu"));
        assertEquals("Sam", userRepository.findByEmailIgnoreCase("SAM@example.edu").orElseThrow().getDisplayName());
        assertFalse(userRepository.existsByEmailIgnoreCase("other@example.edu"));
    }

    @Test
    void studyGroupRepositoryMatchesTrimmedCourseIgnoringCaseInIdOrder() {
        studyGroupRepository.save(new StudyGroup("Second", " CST438 "));
        studyGroupRepository.save(new StudyGroup("Other course", "CST 999"));
        studyGroupRepository.save(new StudyGroup("Third", "cst438"));

        List<StudyGroup> results = studyGroupRepository.searchByCourse("cSt438");

        assertEquals(List.of("Second", "Third"), results.stream().map(StudyGroup::getName).toList());
    }

    @Test
    void membershipRepositoryFindsMembershipsFromEitherSide() {
        User user = userRepository.save(new User("member@example.edu", "Member"));
        StudyGroup group = studyGroupRepository.save(new StudyGroup("Study Buddies", "CST438"));
        membershipRepository.save(new Membership(user, group));

        assertTrue(membershipRepository.existsByUser_IdAndStudyGroup_Id(user.getId(), group.getId()));
        assertEquals(1, membershipRepository.findAllByUser_IdOrderByJoinedAtDesc(user.getId()).size());
        assertEquals(1, membershipRepository.findAllByStudyGroup_IdOrderByJoinedAtAsc(group.getId()).size());
    }

    @Test
    void studySessionRepositoryStoresDurationAndCompletionTimeForUser() {
        User user = userRepository.save(new User("student@example.edu", "Student"));
        Instant completedAt = Instant.parse("2026-11-04T18:30:00Z");
        studySessionRepository.save(new StudySession(user, 1500, completedAt));

        List<StudySession> sessions = studySessionRepository.findAllByUser_IdOrderByCompletedAtDesc(user.getId());

        assertEquals(1, sessions.size());
        assertEquals(1500, sessions.getFirst().getDurationSeconds());
        assertEquals(completedAt, sessions.getFirst().getCompletedAt());
    }
}
