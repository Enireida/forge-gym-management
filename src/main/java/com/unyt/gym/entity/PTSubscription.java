package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * A member's purchased PT package along with the trainer they were
 * matched with and how many sessions remain.
 */
@Entity
@Table(name = "pt_subscriptions")
public class PTSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Staff trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PTPackage ptPackage;

    @Column(name = "sessions_remaining", nullable = false)
    private int sessionsRemaining;

    @Column(name = "sessions_used", nullable = false)
    private int sessionsUsed;

    @Column(name = "start_date")
    private LocalDate startDate = LocalDate.now();

    @Column(nullable = false, length = 20)
    private String status = "Active";

    public PTSubscription() { }

    public PTSubscription(Member member, Staff trainer, PTPackage pkg) {
        this.member = member;
        this.trainer = trainer;
        this.ptPackage = pkg;
        this.sessionsRemaining = pkg.getSessions();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public Staff getTrainer() { return trainer; }
    public void setTrainer(Staff trainer) { this.trainer = trainer; }
    public PTPackage getPtPackage() { return ptPackage; }
    public void setPtPackage(PTPackage ptPackage) { this.ptPackage = ptPackage; }
    public int getSessionsRemaining() { return sessionsRemaining; }
    public void setSessionsRemaining(int sessionsRemaining) { this.sessionsRemaining = sessionsRemaining; }
    public int getSessionsUsed() { return sessionsUsed; }
    public void setSessionsUsed(int sessionsUsed) { this.sessionsUsed = sessionsUsed; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
