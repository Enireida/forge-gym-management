package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * News message broadcast by the admin to all members or staff.
 */
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    /** "all", "members", "staff", "trainers". */
    @Column(nullable = false, length = 30)
    private String audience = "all";

    @Column(nullable = false, length = 20)
    private String priority = "normal";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Announcement() { }

    public Announcement(String title, String message, String audience) {
        this.title = title;
        this.message = message;
        this.audience = audience;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
