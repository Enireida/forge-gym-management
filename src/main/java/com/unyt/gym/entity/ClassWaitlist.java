package com.unyt.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_waitlist", uniqueConstraints = {
    @UniqueConstraint(name = "uk_waitlist_class_member", columnNames = {"class_id", "member_id"})
})
public class ClassWaitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private GymClass gymClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "notified")
    private boolean notified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ClassWaitlist() {}

    public ClassWaitlist(GymClass gymClass, Member member, int position) {
        this.gymClass = gymClass;
        this.member = member;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GymClass getGymClass() { return gymClass; }
    public void setGymClass(GymClass gymClass) { this.gymClass = gymClass; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
