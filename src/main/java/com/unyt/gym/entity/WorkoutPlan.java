package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Curated workout programme — e.g. "Beginner Full-Body", "Strength
 * Hypertrophy", "Cardio Burner". Groups together a handful of exercises
 * that members can browse and follow.
 */
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    /** Beginner / Intermediate / Advanced. */
    @Column(nullable = false, length = 30)
    private String level = "Beginner";

    /** Strength / Hypertrophy / Cardio / Mobility / Weight loss / ... */
    @Column(nullable = false, length = 60)
    private String goal;

    /** Suggested duration in weeks. */
    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    /** Sessions per week. */
    @Column(name = "sessions_per_week")
    private Integer sessionsPerWeek;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "workout_plan_exercises",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "exercise_id"))
    private Set<Exercise> exercises = new HashSet<>();

    public WorkoutPlan() { }

    public WorkoutPlan(String name, String level, String goal, String description) {
        this.name = name;
        this.level = level;
        this.goal = goal;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }
    public Integer getSessionsPerWeek() { return sessionsPerWeek; }
    public void setSessionsPerWeek(Integer sessionsPerWeek) { this.sessionsPerWeek = sessionsPerWeek; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Set<Exercise> getExercises() { return exercises; }
    public void setExercises(Set<Exercise> exercises) { this.exercises = exercises; }
}
