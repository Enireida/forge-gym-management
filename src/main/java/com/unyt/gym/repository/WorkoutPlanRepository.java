package com.unyt.gym.repository;

import com.unyt.gym.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByLevel(String level);
    List<WorkoutPlan> findByGoal(String goal);
}
