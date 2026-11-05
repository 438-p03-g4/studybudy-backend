package com.example.studybudybackend.backend.domain

import jakarta.persistence.*
import java.time.Instant

// A user's membership in a study group
@Entity
@Table(
    name = "membership",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_membership_user_group", columnNames = ["user_id", "study_group_id"]
        ),
    ],
)
class Membership(
    // DB generated PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Many memberships can point to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_membership_user")
    )
    var user: User = User(),

    // Many memberships can point to one group
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "study_group_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_membership_study_group")
    )
    var studyGroup: StudyGroup = StudyGroup(),

    // The user's role in this group
    @Column(nullable = false, length = 30)
    var role: String = "MEMBER",

    // When new student joins the group
    @Column(name = "joined_at", nullable = false, updatable = false)
    var joinedAt: Instant = Instant.now(),
)
