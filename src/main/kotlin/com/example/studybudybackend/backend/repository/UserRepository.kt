package com.example.studybudybackend.backend.repository

import com.example.studybudybackend.backend.domain.User
import org.springframework.data.jpa.repository.JpaRepository

// DB operations and account lookup by email
interface UserRepository : JpaRepository<User, Long> {
    // Finds a user ignoring email capitalization
    fun findByEmailIgnoreCase(email: String): User?

    // Checks whether an account already uses this email
    fun existsByEmailIgnoreCase(email: String): Boolean
}