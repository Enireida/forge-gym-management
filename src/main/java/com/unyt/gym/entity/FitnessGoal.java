package com.unyt.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitness_goals")
public class FitnessGoal {

    public enum GoalType {
        WEIGHT_LOSS, WEIGHT_GAIN, WORKOUT_FREQUENCY, DISTANCE, CALORIES_BURNED, CUSTOM
    }

    public enum GoalStatus {
        ACTIVE, COMPLETED, ABANDONED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "target_value", nullable = false)
    private Double targetValue;

    @Column(name = "current_value")
    private Double currentValue = 0.0;

    @Column(length = 30)
    private String unit;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public FitnessGoal() {}

    @Transient
    public int getProgressPercent() {
        if (targetValue == null || targetValue == 0) return 0;
        double pct = (currentValue != null ? currentValue : 0) / targetValue * 100;
        return (int) Math.min(100, Math.max(0, pct));
    }

    @Transient
    public boolean isOverdue() {
        return targetDate != null && targetDate.isBefore(LocalDate.now()) && status == GoalStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public GoalType getGoalType() { return goalType; }
    public void setGoalType(GoalType goalType) { this.goalType = goalType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Double getTargetValue() { return targetValue; }
    public void setTargetValue(Double targetValue) { this.targetValue = targetValue; }
    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
