package com.example.studybudybackend.backend.repository

import com.example.studybudybackend.backend.domain.Membership
import com.example.studybudybackend.backend.domain.StudyGroup
import com.example.studybudybackend.backend.domain.StudySession
import com.example.studybudybackend.backend.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import java.time.Instant

// Repository tests use an in-memory PostgreSQL-compatible database, never Neon.
@DataJpaTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:studybudy-repository;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTests @Autowired constructor(
    private val userRepository: UserRepository,
    private val studyGroupRepository: StudyGroupRepository,
    private val membershipRepository: MembershipRepository,
    private val studySessionRepository: StudySessionRepository,
) {
    @Test
    fun `user repository finds email without case sensitivity`() {
        userRepository.save(User(email = "sam@example.edu", displayName = "Sam"))

        assertTrue(userRepository.existsByEmailIgnoreCase("SAM@example.edu"))
        assertEquals("Sam", userRepository.findByEmailIgnoreCase("SAM@example.edu")?.displayName)
        assertFalse(userRepository.existsByEmailIgnoreCase("other@example.edu"))
    }

    @Test
    fun `study group repository matches a trimmed course ignoring case in id order`() {
        studyGroupRepository.save(StudyGroup(name = "Second", course = " CST438 "))
        studyGroupRepository.save(StudyGroup(name = "Other course", course = "CST 999"))
        studyGroupRepository.save(StudyGroup(name = "Third", course = "cst438"))

        val results = studyGroupRepository.searchByCourse("cSt438")

        assertEquals(listOf("Second", "Third"), results.map { it.name })
    }

    @Test
    fun `membership repository finds memberships from either side`() {
        val user = userRepository.save(User(email = "member@example.edu", displayName = "Member"))
        val group = studyGroupRepository.save(StudyGroup(name = "Study Buddies", course = "CST438"))
        membershipRepository.save(Membership(user = user, studyGroup = group))

        assertTrue(membershipRepository.existsByUser_IdAndStudyGroup_Id(user.id!!, group.id!!))
        assertEquals(1, membershipRepository.findAllByUser_IdOrderByJoinedAtDesc(user.id!!).size)
        assertEquals(1, membershipRepository.findAllByStudyGroup_IdOrderByJoinedAtAsc(group.id!!).size)
    }

    @Test
    fun `study session repository stores duration and completion time for its user`() {
        val user = userRepository.save(User(email = "student@example.edu", displayName = "Student"))
        val completedAt = Instant.parse("2026-11-04T18:30:00Z")

        studySessionRepository.save(
            StudySession(user = user, durationSeconds = 1500, completedAt = completedAt)
        )

        val sessions = studySessionRepository.findAllByUser_IdOrderByCompletedAtDesc(user.id!!)
        assertEquals(1, sessions.size)
        assertEquals(1500, sessions.single().durationSeconds)
        assertEquals(completedAt, sessions.single().completedAt)
    }
}
