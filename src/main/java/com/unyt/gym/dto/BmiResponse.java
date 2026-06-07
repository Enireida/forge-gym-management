package com.unyt.gym.dto;

/** Response body returned by the BMI REST endpoint. */
public class BmiResponse {

    private double bmi;
    private String category;
    private String advice;

    public BmiResponse(double bmi, String category, String advice) {
        this.bmi = bmi;
        this.category = category;
        this.advice = advice;
    }

    public double getBmi() { return bmi; }
    public String getCategory() { return category; }
    public String getAdvice() { return advice; }
}
