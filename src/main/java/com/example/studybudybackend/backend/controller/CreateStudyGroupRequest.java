package com.example.studybudybackend.backend.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input for creating a course-specific study group.
 */
public record CreateStudyGroupRequest(
        @NotBlank(message = "Group name must not be blank")
        @Size(max = 120, message = "Group name must not exceed 120 characters")
        String name,

        @NotBlank(message = "Course must not be blank")
        @Size(max = 40, message = "Course must not exceed 40 characters")
        String course
) {
}
