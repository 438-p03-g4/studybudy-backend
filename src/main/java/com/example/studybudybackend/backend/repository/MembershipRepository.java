package com.example.studybudybackend.backend.repository;

import com.example.studybudybackend.backend.domain.Membership;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @EntityGraph(attributePaths = "studyGroup")
    List<Membership> findAllByUser_IdOrderByJoinedAtDesc(Long userId);

    @EntityGraph(attributePaths = "user")
    List<Membership> findAllByStudyGroup_IdOrderByJoinedAtAsc(Long studyGroupId);

    boolean existsByUser_IdAndStudyGroup_Id(Long userId, Long studyGroupId);
}
