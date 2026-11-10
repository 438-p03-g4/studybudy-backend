package com.example.studybudybackend.backend.repository;

import com.example.studybudybackend.backend.domain.StudyGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    // Matches the whole course value ignoring case/whitespace, in stable ID order.
    @Query("SELECT g FROM StudyGroup g WHERE lower(trim(g.course)) = lower(trim(:course)) ORDER BY g.id ASC")
    List<StudyGroup> searchByCourse(@Param("course") String course);
}
