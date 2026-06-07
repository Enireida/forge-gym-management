package com.unyt.gym.controller.api;

import com.unyt.gym.dto.BmiRequest;
import com.unyt.gym.dto.BmiResponse;
import com.unyt.gym.exception.GymException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that computes the Body Mass Index for a given height and
 * weight. JSON in, JSON out — exactly the shape the requirement document
 * (R8) asks for.
 *
 * <pre>
 * POST /api/bmi
 * { "weightKg": 78.5, "heightCm": 180 }
 *
 *   200 OK
 *   { "bmi": 24.2, "category": "Normal", "advice": "Keep it up!" }
 * </pre>
 */
@RestController
@RequestMapping("/api/bmi")
public class BmiRestController {

    @PostMapping
    public ResponseEntity<BmiResponse> calculate(@Valid @RequestBody BmiRequest request) {
        if (request.getHeightCm() <= 0 || request.getWeightKg() <= 0) {
            throw new GymException("height and weight must both be positive", 400);
        }
        double heightM = request.getHeightCm() / 100.0;
        double bmi = Math.round(request.getWeightKg() / (heightM * heightM) * 10.0) / 10.0;

        String category;
        String advice;
        if (bmi < 18.5) {
            category = "Underweight";
            advice = "Consider a strength-training programme and a calorie surplus.";
        } else if (bmi < 25.0) {
            category = "Normal";
            advice = "Excellent! Maintain your routine.";
        } else if (bmi < 30.0) {
            category = "Overweight";
            advice = "Add 30 minutes of cardio four times a week.";
        } else {
            category = "Obese";
            advice = "Speak with your trainer about a tailored plan.";
        }
        return ResponseEntity.ok(new BmiResponse(bmi, category, advice));
    }
}
