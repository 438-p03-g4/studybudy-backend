package com.example.studybudybackend.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "study_group_message")
public class StudyGroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_study_group"))
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_user"))
    private User user;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected StudyGroupMessage() {
        // Required by JPA.
    }

    public StudyGroupMessage(StudyGroup studyGroup, User user, String content) {
        this.studyGroup = studyGroup;
        this.user = user;
        this.content = content;
    }

    public Long getId() { return id; }
    public StudyGroup getStudyGroup() { return studyGroup; }
    public User getUser() { return user; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
