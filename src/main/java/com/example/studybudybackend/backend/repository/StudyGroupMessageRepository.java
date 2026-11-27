package com.example.studybudybackend.backend.repository;

import com.example.studybudybackend.backend.domain.StudyGroupMessage;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupMessageRepository extends JpaRepository<StudyGroupMessage, Long> {
    @EntityGraph(attributePaths = "user")
    List<StudyGroupMessage> findAllByStudyGroup_IdOrderByCreatedAtAsc(Long studyGroupId);
}
