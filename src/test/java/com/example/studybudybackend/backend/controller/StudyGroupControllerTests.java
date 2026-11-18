package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyGroupController.class)
@ImportAutoConfiguration(exclude = OAuth2ClientWebSecurityAutoConfiguration.class)
class StudyGroupControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyGroupRepository studyGroupRepository;

    @MockitoBean
    private MembershipRepository membershipRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void returnsMatchingGroups() throws Exception {
        StudyGroup first = new StudyGroup("CST438 Study Group", "CST438");
        first.setId(1L);

        StudyGroup second = new StudyGroup("Backend Group", "CST438");
        second.setId(2L);

        when(studyGroupRepository.searchByCourse("CST438"))
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/groups").param("course", "CST438"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("CST438 Study Group"))
                .andExpect(jsonPath("$[0].course").value("CST438"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(studyGroupRepository).searchByCourse("CST438");
    }

    @Test
    void returnsEmptyListWhenNoGroupsMatch() throws Exception {
        when(studyGroupRepository.searchByCourse("CST999"))
                .thenReturn(List.of());

        mockMvc.perform(get("/groups").param("course", "CST999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsRandomGroupsWhenQueryIsMissing() throws Exception {
        StudyGroup group = new StudyGroup("CST438 Study Group", "CST438");
        group.setId(1L);
        when(studyGroupRepository.findAll()).thenReturn(List.of(group));

        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("CST438 Study Group"));

        verify(studyGroupRepository).findAll();
    }

    @Test
    void returnsRandomGroupsWhenQueryIsBlank() throws Exception {
        when(studyGroupRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/groups").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsGroupsMatchingNameOrCourse() throws Exception {
        StudyGroup group = new StudyGroup("Backend Group", "CST438");
        group.setId(4L);
        when(studyGroupRepository.searchByQuery("backend"))
                .thenReturn(List.of(group));

        mockMvc.perform(get("/groups").param("query", " backend "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Backend Group"));

        verify(studyGroupRepository).searchByQuery("backend");
    }

    @Test
    void createsGroupAndAddsAuthenticatedUserAsOwner() throws Exception {
        User creator = new User("student@example.com", "Student");
        creator.setId(7L);

        StudyGroup saved = new StudyGroup(
                "CST438 Group",
                "CST438",
                "https://example.com/icon.png",
                "Weekly study sessions"
        );
        saved.setId(3L);

        when(userRepository.findByEmailIgnoreCase("student@example.com"))
                .thenReturn(java.util.Optional.of(creator));
        when(studyGroupRepository.save(org.mockito.ArgumentMatchers.any(StudyGroup.class)))
                .thenReturn(saved);

        org.springframework.security.oauth2.core.user.OAuth2User oauth2User =
                org.mockito.Mockito.mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("student@example.com");

        StudyGroupController.CreateGroupRequest request = new StudyGroupController.CreateGroupRequest(
                "CST438 Group",
                "CST438",
                "https://example.com/icon.png",
                "Weekly study sessions"
        );

        StudyGroupController controller = new StudyGroupController(
                studyGroupRepository,
                membershipRepository,
                userRepository
        );
        var response = controller.createGroup(request, oauth2User);

        org.junit.jupiter.api.Assertions.assertEquals(
                org.springframework.http.HttpStatus.CREATED,
                response.getStatusCode()
        );
        org.junit.jupiter.api.Assertions.assertEquals(3L, response.getBody().id());
        org.junit.jupiter.api.Assertions.assertEquals(
                "https://example.com/icon.png",
                response.getBody().iconUrl()
        );
        org.junit.jupiter.api.Assertions.assertEquals(
                "Weekly study sessions",
                response.getBody().description()
        );

        org.mockito.ArgumentCaptor<Membership> membershipCaptor =
                org.mockito.ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("OWNER", membershipCaptor.getValue().getRole());
        org.junit.jupiter.api.Assertions.assertSame(creator, membershipCaptor.getValue().getUser());
    }
}
