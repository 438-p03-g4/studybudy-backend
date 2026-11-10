package com.example.studybudybackend.backend.repository;

import com.example.studybudybackend.backend.domain.StudySession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findAllByUser_IdOrderByCompletedAtDesc(Long userId);
}
