package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Workout session log synced from Apple Watch / HealthKit.
 */
@Entity
@Table(name = "workout_log")
public class WorkoutLog {

    public enum WorkoutType {
        STRENGTH, CARDIO, HIIT, YOGA, CYCLING, RUNNING, SWIMMING, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private WorkoutType workoutType = WorkoutType.OTHER;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationSeconds;

    private Double activeCalories;

    private Double totalCalories;

    private Integer avgHeartRate;

    private Integer maxHeartRate;

    private Integer minHeartRate;

    private Double distanceMeters;

    private Integer stepCount;

    @Column(length = 50)
    private String source = "apple_watch";

    @Column(unique = true, length = 64)
    private String healthKitUUID;

    private LocalDateTime syncedAt = LocalDateTime.now();

    @Column(length = 500)
    private String notes;

    public WorkoutLog() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public WorkoutType getWorkoutType() { return workoutType; }
    public void setWorkoutType(WorkoutType workoutType) { this.workoutType = workoutType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Double getActiveCalories() { return activeCalories; }
    public void setActiveCalories(Double activeCalories) { this.activeCalories = activeCalories; }

    public Double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Double totalCalories) { this.totalCalories = totalCalories; }

    public Integer getAvgHeartRate() { return avgHeartRate; }
    public void setAvgHeartRate(Integer avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    public Integer getMaxHeartRate() { return maxHeartRate; }
    public void setMaxHeartRate(Integer maxHeartRate) { this.maxHeartRate = maxHeartRate; }

    public Integer getMinHeartRate() { return minHeartRate; }
    public void setMinHeartRate(Integer minHeartRate) { this.minHeartRate = minHeartRate; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Integer getStepCount() { return stepCount; }
    public void setStepCount(Integer stepCount) { this.stepCount = stepCount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getHealthKitUUID() { return healthKitUUID; }
    public void setHealthKitUUID(String healthKitUUID) { this.healthKitUUID = healthKitUUID; }

    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
