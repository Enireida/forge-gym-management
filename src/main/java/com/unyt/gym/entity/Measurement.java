package com.unyt.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Body measurement snapshot taken by the trainer for a specific member.
 * Used to plot the member's progress chart.
 */
@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "body_fat_pct")
    private Double bodyFatPct;

    @Column(name = "muscle_mass")
    private Double muscleMass;

    private Double chest;
    private Double waist;
    private Double hips;

    @Column(length = 500)
    private String notes;

    public Measurement() { }

    public Measurement(Member member, LocalDate date, Double weightKg, Double heightCm) {
        this.member = member;
        this.date = date;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public Double getBodyFatPct() { return bodyFatPct; }
    public void setBodyFatPct(Double bodyFatPct) { this.bodyFatPct = bodyFatPct; }
    public Double getMuscleMass() { return muscleMass; }
    public void setMuscleMass(Double muscleMass) { this.muscleMass = muscleMass; }
    public Double getChest() { return chest; }
    public void setChest(Double chest) { this.chest = chest; }
    public Double getWaist() { return waist; }
    public void setWaist(Double waist) { this.waist = waist; }
    public Double getHips() { return hips; }
    public void setHips(Double hips) { this.hips = hips; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
