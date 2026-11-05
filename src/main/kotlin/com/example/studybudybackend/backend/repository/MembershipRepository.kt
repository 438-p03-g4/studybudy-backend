package com.example.studybudybackend.backend.repository

import com.example.studybudybackend.backend.domain.Membership
import org.springframework.data.jpa.repository.JpaRepository

// DB operations and lookups for group membership
interface MembershipRepository : JpaRepository<Membership, Long> {
    // Gets a user's memberships, newest first
    fun findAllByUser_IdOrderByJoinedAtDesc(userId: Long): List<Membership>

    // Gets the members of a group, earliest first
    fun findAllByStudyGroup_IdOrderByJoinedAtAsc(
        studyGroupId: Long
    ): List<Membership>

    // Checks whether a user has already joined a group
    fun existsByUser_IdAndStudyGroup_Id(
        userId: Long,
        studyGroupId: Long
    ): Boolean
}