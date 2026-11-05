package com.example.studybudybackend.backend.domain

import jakarta.persistence.*
import java.time.Instant

// A study session belongs to one user
@Entity
@Table(name = "study_session")
class StudySession(
    // DB generated PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Many study sessions can belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_study_session_user"),
    )
    var user: User = User(),

    // Session length, stored in seconds (can change to minutes if wanted)
    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,

    // Time the session was completed
    // Null -> the session is still active
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
)