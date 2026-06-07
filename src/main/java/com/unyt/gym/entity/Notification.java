package com.unyt.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotifType {
        MEMBERSHIP_EXPIRY, CLASS_REMINDER, GOAL_ACHIEVED, NEW_CLASS,
        TRAINER_SESSION, ANNOUNCEMENT, SYSTEM, WAITLIST_AVAILABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "notif_type", nullable = false, length = 30)
    private NotifType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String message;

    @Column(name = "link_url", length = 200)
    private String linkUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(Member member, NotifType type, String title, String message) {
        this.member = member;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public Notification(Member member, NotifType type, String title, String message, String linkUrl) {
        this(member, type, title, message);
        this.linkUrl = linkUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public NotifType getType() { return type; }
    public void setType(NotifType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
