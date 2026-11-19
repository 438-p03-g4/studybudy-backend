package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:group-creation;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)

class StudyGroupPersistenceTests {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsGroupAndOwnerMembershipAndFindsGroupByCourse() {
        User creator = userRepository.save(
                new User("linus-test@example.com", "Linus")
        );
        Long creatorId = creator.getId();

        var principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "linus-test@example.com"),
                "email"
        );

        StudyGroupController controller = new StudyGroupController(
                studyGroupRepository,
                membershipRepository,
                userRepository
        );

        var request = new StudyGroupController.CreateGroupRequest(
                "  Backend Study Group  ",
                "  CST438  ",
                "  https://example.com/icon.png  ",
                "  Weekly backend sessions  "
        );

        var response = controller.createGroup(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Long groupId = response.getBody().id();

        assertNotNull(groupId);
        assertTrue(groupId > 0);

        entityManager.flush();
        entityManager.clear();

        StudyGroup persistedGroup = studyGroupRepository
                .findById(groupId)
                .orElseThrow();

        assertEquals("Backend Study Group", persistedGroup.getName());
        assertEquals("CST438", persistedGroup.getCourse());
        assertEquals(
                "https://example.com/icon.png",
                persistedGroup.getIconUrl()
        );
        assertEquals(
                "Weekly backend sessions",
                persistedGroup.getDescription()
        );

        List<Membership> ownerMemberships = membershipRepository
                .findAll()
                .stream()
                .filter(membership ->
                        groupId.equals(membership.getStudyGroup().getId()))
                .toList();

        assertEquals(1, ownerMemberships.size());

        Membership ownerMembership = ownerMemberships.getFirst();

        assertEquals("OWNER", ownerMembership.getRole());
        assertEquals(creatorId, ownerMembership.getUser().getId());

        var searchResults = studyGroupRepository
                .searchByCourse("cst438");

        assertTrue(searchResults.stream()
                .anyMatch(group -> groupId.equals(group.getId())));
    }
}
