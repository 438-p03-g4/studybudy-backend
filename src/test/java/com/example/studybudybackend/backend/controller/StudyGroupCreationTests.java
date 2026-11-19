package com.example.studybudybackend.backend.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.example.studybudybackend.backend.domain.Membership;
import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.domain.User;
import com.example.studybudybackend.backend.repository.MembershipRepository;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import com.example.studybudybackend.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyGroupController.class)
@ImportAutoConfiguration(
        exclude = OAuth2ClientWebSecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)

@Import(StudyGroupCreationTests.PrincipalTestConfiguration.class)

class StudyGroupCreationTests {
    @TestConfiguration(proxyBeanMethods = false)
    static class PrincipalTestConfiguration implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(
                List<HandlerMethodArgumentResolver> resolvers
        ) {
            resolvers.add(
                    new AuthenticationPrincipalArgumentResolver()
            );
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyGroupRepository studyGroupRepository;

    @MockitoBean
    private MembershipRepository membershipRepository;

    @MockitoBean
    private UserRepository userRepository;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = new User("linus@example.com", "Linus");
        creator.setId(7L);

        when(userRepository.findByEmailIgnoreCase("linus@example.com"))
                .thenReturn(Optional.of(creator));

        when(studyGroupRepository.save(any(StudyGroup.class)))
                .thenAnswer(invocation -> {
                    StudyGroup group = invocation.getArgument(0);
                    group.setId(42L);
                    return group;
                });

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        var principal = new DefaultOAuth2User(
                authorities,
                Map.of("email", "linus@example.com"),
                "email"
        );

        var authentication = new OAuth2AuthenticationToken(
                principal,
                authorities,
                "google"
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsGroupAndOwnerMembership() throws Exception {
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
                .andExpect(jsonPath("$.name")
                        .value("CST438 Study Group"))
                .andExpect(jsonPath("$.course").value("CST438"));

        var groupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository).save(groupCaptor.capture());

        var membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());

        Membership membership = membershipCaptor.getValue();

        assertSame(creator, membership.getUser());
        assertSame(groupCaptor.getValue(), membership.getStudyGroup());
        assertEquals("OWNER", membership.getRole());
    }

    @Test
    void trimsAllFieldsBeforeSaving() throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Study Buddies  ",
                                  "course": "  CST438  ",
                                  "iconUrl": "  https://example.com/icon.png  ",
                                  "description": "  Weekly sessions  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Study Buddies"))
                .andExpect(jsonPath("$.course").value("CST438"))
                .andExpect(jsonPath("$.iconUrl")
                        .value("https://example.com/icon.png"))
                .andExpect(jsonPath("$.description")
                        .value("Weekly sessions"));

        var captor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository).save(captor.capture());

        assertEquals("Study Buddies", captor.getValue().getName());
        assertEquals("CST438", captor.getValue().getCourse());
        assertEquals(
                "https://example.com/icon.png",
                captor.getValue().getIconUrl()
        );
        assertEquals(
                "Weekly sessions",
                captor.getValue().getDescription()
        );
    }

    @Test
    void storesBlankOptionalFieldsAsNull() throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Study Buddies",
                                  "course": "CST438",
                                  "iconUrl": "   ",
                                  "description": "   "
                                }
                                """))
                .andExpect(status().isCreated());

        var captor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository).save(captor.capture());

        assertNull(captor.getValue().getIconUrl());
        assertNull(captor.getValue().getDescription());
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
    void rejectsOversizedName() throws Exception {
        assertRejected("""
                {"name": "%s", "course": "CST438"}
                """.formatted("A".repeat(121)));
    }

    @Test
    void rejectsOversizedCourse() throws Exception {
        assertRejected("""
                {"name": "Study Buddies", "course": "%s"}
                """.formatted("A".repeat(41)));
    }

    @Test
    void rejectsOversizedIconUrl() throws Exception {
        assertRejected("""
                {"name": "Study Buddies", "course": "CST438", "iconUrl": "%s"}
                """.formatted("A".repeat(501)));
    }

    @Test
    void rejectsOversizedDescription() throws Exception {
        assertRejected("""
                {"name": "Study Buddies", "course": "CST438", "description": "%s"}
                """.formatted("A".repeat(1001)));
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

        verifyNoInteractions(studyGroupRepository, membershipRepository);
    }

    private void assertRejected(String body) throws Exception {
        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        verifyNoInteractions(studyGroupRepository, membershipRepository);
    }
}
