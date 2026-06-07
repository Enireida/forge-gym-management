package com.unyt.gym.entity;

import jakarta.persistence.*;

/**
 * Single exercise entry in the gym's exercise library.
 */
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "muscle_group", nullable = false, length = 60)
    private String muscleGroup;

    @Column(nullable = false, length = 30)
    private String difficulty = "Beginner";

    @Column(length = 60)
    private String equipment;

    @Column(length = 2000)
    private String instructions;

    /** URL to a photo / animation of the exercise (Unsplash, gym CDN, ...). */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Suggested sets x reps (e.g. "3 x 10-12"). */
    @Column(length = 30)
    private String setsReps;

    /** Suggested rest between sets, in seconds. */
    @Column(name = "rest_seconds")
    private Integer restSeconds;

    public Exercise() { }

    public Exercise(String name, String muscleGroup, String difficulty,
                    String equipment, String instructions) {
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.difficulty = difficulty;
        this.equipment = equipment;
        this.instructions = instructions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSetsReps() { return setsReps; }
    public void setSetsReps(String setsReps) { this.setsReps = setsReps; }
    public Integer getRestSeconds() { return restSeconds; }
    public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }
}
