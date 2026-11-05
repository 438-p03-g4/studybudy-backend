package com.example.studybudy-backend.backend.domain

import jakarta.persistence.*
import java.time.Instant

// One account in the app
// A user can join groups and complete many study sessions.
@Entity
@Table(name = "app_user")
class User(
    // DB generated PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Account's email
    @Column(nullable = false, length = 320)
    var email: String = "",

    // Name shown to other users
    @Column(name = "display_name", nullable = false, length = 120)
    var displayName: String = "",

    // When the user account was created
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    // One user can have multiple group memberships
    // The Membership entity owns the database relationship
    @OneToMany(mappedBy = "user")
    var memberships: MutableSet<Membership> = linkedSetOf(),

    // One user can have multiple study sessions
    @OneToMany(mappedBy = "user")
    var studySessions: MutableSet<StudySession> = linkedSetOf(),
)