package com.unyt.gym.repository;

import com.unyt.gym.entity.FitnessGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Long> {

    List<FitnessGoal> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<FitnessGoal> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, FitnessGoal.GoalStatus status);

    long countByMemberIdAndStatus(Long memberId, FitnessGoal.GoalStatus status);
}
