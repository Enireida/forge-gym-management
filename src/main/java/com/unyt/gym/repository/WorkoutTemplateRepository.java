package com.unyt.gym.repository;

import com.unyt.gym.entity.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findByMemberIdOrderByTimesUsedDesc(Long memberId);
}
