package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class StudyGroupController {

    private final StudyGroupRepository studyGroupRepository;

    public StudyGroupController(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    @GetMapping
    public ResponseEntity<List<StudyGroupSummary>> searchGroups(
            @RequestParam(required = false) String course) {

        if (course == null || course.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<StudyGroupSummary> results = studyGroupRepository
                .searchByCourse(course)
                .stream()
                .map(group -> new StudyGroupSummary(
                        group.getId(),
                        group.getName(),
                        group.getCourse()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }

    public record StudyGroupSummary(
            Long id,
            String name,
            String course
    ) {
    }
}