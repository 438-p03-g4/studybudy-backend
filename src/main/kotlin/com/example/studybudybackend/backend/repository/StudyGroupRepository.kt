package com.example.studybudybackend.backend.repository

import com.example.studybudybackend.backend.domain.StudyGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

// DB operations and group search by course
interface StudyGroupRepository : JpaRepository<StudyGroup, Long> {
    // Matches the whole course value, ignoring case and whitespaces
    // Orders results by ID so the order is stable
    @Query(
        """
        SELECT g FROM StudyGroup g
        WHERE lower(trim(g.course)) = lower(trim(:course))
        ORDER BY g.id asc
        """
    )
    fun searchByCourse(@Param("course") course: String): List<StudyGroup>
}