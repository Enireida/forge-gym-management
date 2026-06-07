package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A single completed personal-training session, logged by the trainer.
 *
 * <p>Each row decrements the {@code sessionsRemaining} counter on the
 * parent {@link PTSubscription}.</p>
 */
@Entity
@Table(name = "pt_sessions")
public class PTSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private PTSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Staff trainer;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate = LocalDate.now();

    @Column(name = "session_time", nullable = false)
    private LocalTime sessionTime = LocalTime.now();

    @Column(length = 1000)
    private String notes;

    /** Type: Strength / Cardio / Yoga / etc. */
    @Column(length = 60)
    private String type;

    public PTSession() { }

    public PTSession(PTSubscription subscription, Staff trainer, String type, String notes) {
        this.subscription = subscription;
        this.trainer = trainer;
        this.type = type;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PTSubscription getSubscription() { return subscription; }
    public void setSubscription(PTSubscription subscription) { this.subscription = subscription; }
    public Staff getTrainer() { return trainer; }
    public void setTrainer(Staff trainer) { this.trainer = trainer; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public LocalTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalTime sessionTime) { this.sessionTime = sessionTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
