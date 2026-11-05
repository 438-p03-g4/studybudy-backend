package com.example.studybudybackend.backend.domain

import jakarta.persistence.*

// A study group associated with a course
@Entity
@Table(name = "study_group")
class StudyGroup(
    // DB generated PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // Group name: e.g.: "CST438 Study Group"
    @Column(nullable = false, length = 120)
    var name: String = "",

    // Course identifier used to find groups: e.g.: "CST438"
    @Column(nullable = false, length = 40)
    var course: String = "",

    // A group can have many memberships
    // Each membership connects this group to one user
    @OneToMany(mappedBy = "studyGroup")
    var memberships: MutableSet<Membership> = linkedSetOf(),
)