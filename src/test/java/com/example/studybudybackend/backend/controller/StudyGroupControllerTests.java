package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class StudyGroupControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyGroupRepository studyGroupRepository;

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
    void rejectsMissingCourse() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankCourse() throws Exception {
        mockMvc.perform(get("/groups").param("course", "   "))
                .andExpect(status().isBadRequest());
    }
}