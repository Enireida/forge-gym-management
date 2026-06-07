package com.unyt.gym.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request body for the BMI REST endpoint. */
public class BmiRequest {

    @NotNull
    @Positive
    private Double weightKg;

    @NotNull
    @Positive
    private Double heightCm;

    public BmiRequest() { }

    public BmiRequest(Double weightKg, Double heightCm) {
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
}
