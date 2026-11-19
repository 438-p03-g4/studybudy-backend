package com.example.studybudybackend.backend.controller;

import com.example.studybudybackend.backend.domain.StudyGroup;
import com.example.studybudybackend.backend.repository.StudyGroupRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Creates a study group from validated input.
     */
    @PostMapping
    public ResponseEntity<StudyGroupSummary> createGroup(
            @Valid @RequestBody CreateStudyGroupRequest request) {

        StudyGroup group = new StudyGroup(
                request.name().strip(),
                request.course().strip()
        );

        StudyGroup savedGroup = studyGroupRepository.save(group);

        StudyGroupSummary response = new StudyGroupSummary(
                savedGroup.getId(),
                savedGroup.getName(),
                savedGroup.getCourse()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Finds study groups matching the requested course.
     */
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