package com.example.studybudybackend.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.HashSet;
import java.util.Set;

// A study group associated with a course.
@Entity
@Table(name = "study_group")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String course;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(length = 1000)
    private String description;

    // A group can have many memberships; each membership refers to one user.
    @OneToMany(mappedBy = "studyGroup")
    private Set<Membership> memberships = new HashSet<>();

    protected StudyGroup() {
        // Required by JPA.
    }

    public StudyGroup(String name, String course) {
        this.name = name;
        this.course = course;
    }

    public StudyGroup(String name, String course, String iconUrl, String description) {
        this.name = name;
        this.course = course;
        this.iconUrl = iconUrl;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(Set<Membership> memberships) {
        this.memberships = memberships;
    }
}
