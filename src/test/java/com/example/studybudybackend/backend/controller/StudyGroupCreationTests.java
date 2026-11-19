package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests group creation and validation independently of authentication.
 * JWT authentication requires separate tests with security filters enabled.
 */
@WebMvcTest(StudyGroupController.class)
@ImportAutoConfiguration(
        exclude = OAuth2ClientWebSecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StudyGroupCreationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyGroupRepository studyGroupRepository;

    @Test
    void createsGroupAndReturnsGeneratedId() throws Exception {
        when(studyGroupRepository.save(any(StudyGroup.class)))
                .thenAnswer(invocation -> {
                    StudyGroup group = invocation.getArgument(0);
                    group.setId(42L);
                    return group;
                });

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CST438 Study Group",
                                  "course": "CST438"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("CST438 Study Group"))
                .andExpect(jsonPath("$.course").value("CST438"));

        ArgumentCaptor<StudyGroup> captor =
                ArgumentCaptor.forClass(StudyGroup.class);

        verify(studyGroupRepository).save(captor.capture());

        assertEquals("CST438 Study Group", captor.getValue().getName());
        assertEquals("CST438", captor.getValue().getCourse());
    }

    @Test
    void trimsNameAndCourseBeforeSaving() throws Exception {
        when(studyGroupRepository.save(any(StudyGroup.class)))
                .thenAnswer(invocation -> {
                    StudyGroup group = invocation.getArgument(0);
                    group.setId(1L);
                    return group;
                });

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Study Buddies  ",
                                  "course": "  CST438  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Study Buddies"))
                .andExpect(jsonPath("$.course").value("CST438"));

        ArgumentCaptor<StudyGroup> captor =
                ArgumentCaptor.forClass(StudyGroup.class);

        verify(studyGroupRepository).save(captor.capture());

        assertEquals("Study Buddies", captor.getValue().getName());
        assertEquals("CST438", captor.getValue().getCourse());
    }

    @Test
    void rejectsMissingName() throws Exception {
        assertRejected("""
                {"course": "CST438"}
                """);
    }

    @Test
    void rejectsBlankName() throws Exception {
        assertRejected("""
                {"name": "   ", "course": "CST438"}
                """);
    }

    @Test
    void rejectsMissingCourse() throws Exception {
        assertRejected("""
                {"name": "Study Buddies"}
                """);
    }

    @Test
    void rejectsBlankCourse() throws Exception {
        assertRejected("""
                {"name": "Study Buddies", "course": "   "}
                """);
    }

    @Test
    void rejectsNameLongerThan120Characters() throws Exception {
        assertRejected("""
                {"name": "%s", "course": "CST438"}
                """.formatted("A".repeat(121)));
    }

    @Test
    void rejectsCourseLongerThan40Characters() throws Exception {
        assertRejected("""
                {"name": "Study Buddies", "course": "%s"}
                """.formatted("A".repeat(41)));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        assertRejected("{");
    }

    @Test
    void rejectsMissingBody() throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }

    private void assertRejected(String body) throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }
}