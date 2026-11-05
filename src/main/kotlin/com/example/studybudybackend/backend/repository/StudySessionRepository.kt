package com.example.studybudybackend.backend.repository

import com.example.studybudybackend.backend.domain.StudySession
import org.springframework.data.jpa.repository.JpaRepository

// DB operations and a user's study history
interface StudySessionRepository : JpaRepository<StudySession, Long> {
    // Gets a user's sessions, recent completed first
    fun findAllByUser_IdOrderByCompletedAtDesc(userId: Long): List<StudySession>
}