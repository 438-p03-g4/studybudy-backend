package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests group creation against an isolated H2 database.
 */
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
    private EntityManager entityManager;

    @Test
    void createdGroupIsPersistedAndCanBeFoundByCourse() {
        StudyGroupController controller =
                new StudyGroupController(studyGroupRepository);

        CreateStudyGroupRequest request =
                new CreateStudyGroupRequest(
                        "  Backend Study Group  ",
                        "  CST438  "
                );

        var response = controller.createGroup(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Long groupId = response.getBody().id();

        assertNotNull(groupId);
        assertTrue(groupId > 0);

        // Write to the database and clear cached entities.
        entityManager.flush();
        entityManager.clear();

        StudyGroup persistedGroup = studyGroupRepository
                .findById(groupId)
                .orElseThrow();

        assertEquals("Backend Study Group", persistedGroup.getName());
        assertEquals("CST438", persistedGroup.getCourse());

        var searchResults =
                studyGroupRepository.searchByCourse("cst438");

        assertEquals(1, searchResults.size());
        assertEquals(groupId, searchResults.getFirst().getId());
    }
}